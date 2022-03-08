package sensors_in_paradise.sonar.custom_views.stickman

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec3
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3D.Cube
import sensors_in_paradise.sonar.custom_views.stickman.object3D.Plane
import sensors_in_paradise.sonar.custom_views.stickman.object3D.Stickman

class StickmanView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val stickman  = Stickman()
    private val lineObjects3DToDraw = arrayOf(Plane(),Cube())

    private val camera = Camera(Vec3(0f,0.5f, 0f),Vec3(0f,1.5f, -3f), Vec3(0f,1f, 0f))
    private var projection = Matrix4x4.project(90f, 1f, -3f, 3f)
    private val textPaint = Paint(0).apply {
        color = Color.WHITE
    }

    private var centerX = 1f
    private var centerY= 1f


    init{
        Thread(){
            while(true){
                Thread.sleep(100)
                camera.rotateAroundCenter()
                Looper.getMainLooper().run { invalidate() }

            }
        }.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            drawText("Camera", 10f, 10f,textPaint )
            drawText("Eye x: ${camera.eye.x} y:${camera.eye.y} z:${camera.eye.z}", 10f, 20f,textPaint )
            drawText("LookAt: ${camera.lookAtMatrix.asString()}", 10f, 30f,textPaint )
            drawText("RotationY: ${Matrix4x4.rotationY(45f).asString()}", 10f, 40f,textPaint )
            drawText("RotationX: ${Matrix4x4.rotationX(45f).asString()}", 10f, 50f,textPaint )
            for(obj in lineObjects3DToDraw){
                obj.draw(canvas, this@StickmanView::project3DPoint)
            }
        }
    }
    private fun project3DPoint(vec4: Vec4): PointF {
        val projected = projection * camera.lookAtMatrix * vec4
        val p = (projected.xy / projected.w + 1f) * 0.5f

        return PointF(p.x*width.toFloat(), height-p.y * width.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = (w/2).toFloat()
        centerY = (h/2).toFloat()

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            Log.d("STICKMAN_VIEW", "onTouchEvent action: ${event.action} axis: ${event.getAxisValue(MotionEvent.AXIS_X)}")
            if(event.action == MotionEvent.ACTION_MOVE){
                // TODO implement camera rotation
                return true
            }

        }
        return false
    }
}
