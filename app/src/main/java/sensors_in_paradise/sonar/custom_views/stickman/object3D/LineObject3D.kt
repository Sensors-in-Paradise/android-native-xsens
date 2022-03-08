package sensors_in_paradise.sonar.custom_views.stickman.object3D

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec2
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class LineObject3D: Object3D() {
    abstract fun getVectorPaint(): Paint
    abstract fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>>
    abstract fun getLinePaint(): Paint


    override fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> PointF) {
        val linePaint = getLinePaint()
        val vectorPaint = getVectorPaint()
        canvas.apply {
            get3DLinesToDraw().forEach {
                val p1 = projectPoint(it.first)
                val p2 = projectPoint(it.second)
                drawLine(p1.x, p1.y, p2.x, p2.y, linePaint)
                drawCircle(p1.x, p1.y, 4f, vectorPaint)
            }
        }
    }
}