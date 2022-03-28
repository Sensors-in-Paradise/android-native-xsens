package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class LineObject3D(
    vertices: Array<Vec4>,
    onObjectChanged: OnObjectChangedInterface? = null
) : Object3D(vertices, onObjectChanged) {
    abstract fun getVectorPaint(): Paint
    abstract fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>>
    abstract fun getLinePaint(lineIndex: Int? = null): Paint

    override fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> PointF) {
        val vectorPaint = getVectorPaint()
        canvas.apply {
            var i = 0
            get3DLinesToDraw().forEach {
                val linePaint = getLinePaint(i)
                val p1 = projectPoint(it.first)
                val p2 = projectPoint(it.second)
                drawLine(p1.x, p1.y, p2.x, p2.y, linePaint)
                drawCircle(p1.x, p1.y, 4f, vectorPaint)
                i++
            }
        }
    }
}
