package sensors_in_paradise.sonar.custom_views.stickman

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec3
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3D.Cube
import sensors_in_paradise.sonar.custom_views.stickman.object3D.Stickman

// camera transformation: file:///C:/Users/tfied/OneDrive/Dokumente/Studium/Archiv/Semester%203/CGS/Script/alle_folien.pdf page 327
class StickmanView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val stickman  = Stickman()
    private val cube = Cube()
    private val camera = Camera(Vec3(0f,0.5f, 0f),Vec3(0f,1f, -5f), Vec3(0f,1f, 0f))
    private var projection = Matrix4x4.project(90f, 1f, -3f, 3f)


    private var centerX = 1f
    private var centerY= 1f
    private val paint = Paint(0).apply {
        color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            cube.get3DLinesToDraw().forEach {
                val p1 = project3DPoint(it.first)
                val p2 = project3DPoint(it.second)
                drawLine(p1.x, p1.y, p2.x, p2.y, paint)
            }
            /*stickman.get3DLinesToDraw().forEach {
                val p1 = project3DPoint(it.first)
                val p2 = project3DPoint(it.second)
                drawLine(p1.x, p1.y, p2.x, p2.y, paint)
            }*/
        }
    }
    private fun project3DPoint(vec4: Vec4): PointF {
        val projected = projection * camera.lookAtMatrix * vec4
        val p = (projected.xy / projected.w + 1f) * 0.5f

        return PointF(p.x*width.toFloat(), p.y * height.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = (w/2).toFloat()
        centerY = (h/2).toFloat()
        projection = Matrix4x4.project(45f, w.toFloat()/h.toFloat(), -3f, 6f)
    }
}
