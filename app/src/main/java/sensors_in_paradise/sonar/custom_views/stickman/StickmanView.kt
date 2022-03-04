package sensors_in_paradise.sonar.custom_views.stickman

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

// camera transformation: file:///C:/Users/tfied/OneDrive/Dokumente/Studium/Archiv/Semester%203/CGS/Script/alle_folien.pdf page 327
class StickmanView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val stickman  = Stickman()





    private var centerX = 1f
    private var centerY= 1f
    private val paint = Paint(0).apply {
        color = Color.BLUE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {

            stickman.get3DLinesToDraw().forEach {
                val p1 = project3DPoint(it.first)
                val p2 = project3DPoint(it.second)
                drawLine(p1.x, p1.y, p2.x, p2.y, paint)
            }
        }
    }
    private fun project3DPoint(point3D: Point3D): PointF {
        val p = (point3D+ 1f) * 0.5f

        return PointF(p.x*width.toFloat(), p.y * height.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = (w/2).toFloat()
        centerY = (h/2).toFloat()

    }
}
