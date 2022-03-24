package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class CoordinateSystem3D  : LineObject3D(
    arrayOf(
       origin, xAxis, yAxis, zAxis
    )
) {

    private val linesToDraw = arrayOf(
        Pair(origin, xAxis),
        Pair(origin, yAxis),
        Pair(origin, zAxis),
    )
    private val head = Cube()

    private val linePaint = Paint(0).apply {
        color = Color.WHITE
        strokeWidth = 6f
    }
    private val vectorPaint = Paint(0).apply {
        color = Color.GREEN
        strokeWidth = 9f
    }

    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return linesToDraw
    }

    override fun getLinePaint(): Paint {
        return linePaint
    }

    override fun getVectorPaint(): Paint {
        return vectorPaint
    }

    override fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> PointF) {
        super.draw(canvas, projectPoint)
        head.draw(canvas, projectPoint)
    }

    companion object {
        private val origin = Vec4(0f, 0f, 0f)
        private val xAxis = Vec4(1f, 0f, 0f)
        private val yAxis = Vec4(0f, 1f, 0f)
        private val zAxis = Vec4(0f, 0f, 1f)

    }
}
