package sensors_in_paradise.sonar.custom_views.stickman

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec3
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3d.Object3D
import sensors_in_paradise.sonar.custom_views.stickman.object3d.OnObjectChangedInterface
import sensors_in_paradise.sonar.custom_views.stickman.object3d.Plane
import sensors_in_paradise.sonar.custom_views.stickman.object3d.Stickman

class Render3DView(context: Context, attrs: AttributeSet) : View(context, attrs), OnObjectChangedInterface {
    private val objects3DToDraw = ArrayList<Object3D>()//arrayListOf(/*Plane().apply{scale(1f, 0f, 1f)}, Cube(), Stickman()*/)

    val camera = Camera(Vec3(0f, 0.5f, 0f), Vec3(0f, 1f, -2f), Vec3(0f, 1f, 0f))
    private var projection = Matrix4x4.project(90f, 1f, 0.1f, 4f)
    private val fpsTextPaint = Paint(0).apply {
        color = Color.WHITE
        textSize = 50F
    }
    private var lastTimeSceneChanged = 0L
    private var centerX = 1f
    private var centerY = 1f
    private var isAnimationThreadInSlowMode = false
    private val animationThread = Thread() {
        while (true) {
            if (System.currentTimeMillis() - lastTimeSceneChanged > 1000L) {
                isAnimationThreadInSlowMode = true
            }
            try {
                Thread.sleep(if (isAnimationThreadInSlowMode) 5000L else 30L)
            } catch (e: InterruptedException) {
                Log.d("StickmanView", "Animation thread interrupted.")
            }
            Looper.getMainLooper().run { invalidate() }
        }
    }.apply { start() }

    private var lastTimeDrawn = 0L
    private val textBounds = Rect()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val fps = 1000 / (System.currentTimeMillis() - lastTimeDrawn)
        val fpsText = "FPS: $fps"

        fpsTextPaint.getTextBounds(fpsText, 0, fpsText.length, textBounds)
        canvas.apply {
            drawText(fpsText, 10f, textBounds.height().toFloat(), fpsTextPaint)

            for (obj in objects3DToDraw) {
                obj.draw(canvas, this@Render3DView::project3DPoint)
            }
        }
        lastTimeDrawn = System.currentTimeMillis()
    }

    private fun project3DPoint(vec4: Vec4): PointF {
        val projected = projection * camera.lookAtMatrix * vec4
        val p = (projected.xy / projected.w + 1f) * 0.5f

        return PointF(p.x * width.toFloat(), height - p.y * width.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
    }

    private var lastEventX = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (event.action == MotionEvent.ACTION_MOVE) {
                val x = event.getAxisValue(MotionEvent.AXIS_X)
                val diff = lastEventX - x
                lastEventX = x
                camera.rotateY(diff / 5f)
                onSceneChanged()
            }
            if (event.action == MotionEvent.ACTION_DOWN) {
                lastEventX = event.getAxisValue(MotionEvent.AXIS_X)
            }
        }
        return true
    }

    private fun onSceneChanged() {
        lastTimeSceneChanged = System.currentTimeMillis()
        if (isAnimationThreadInSlowMode) {
            isAnimationThreadInSlowMode = false
            animationThread.interrupt()
        }
    }
    fun addObject3D(obj: Object3D){
        objects3DToDraw.add(obj)
        onSceneChanged()
    }

    override fun onObjectChanged() {
        onSceneChanged()
    }
}
