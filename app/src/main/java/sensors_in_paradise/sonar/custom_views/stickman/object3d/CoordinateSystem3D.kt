package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class CoordinateSystem3D  : LineObject3D(
    arrayOf(
       origin, xAxis, yAxis, zAxis
    ) + sensorBoundingBox.vertices
) {

    private val linesToDraw = arrayOf(
        Pair(vertices[0], vertices[1]),
        Pair(vertices[0], vertices[2]),
        Pair(vertices[0], vertices[3]),
    )


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
        sensorBoundingBox.draw(canvas, projectPoint)
        super.draw(canvas, projectPoint)


    }

    companion object {
        private val origin = Vec4(0f, 0f, 0f)
        private val xAxis = Vec4(1f, 0f, 0f)
        private val yAxis = Vec4(0f, 1f, 0f)
        private val zAxis = Vec4(0f, 0f, 1f)
        private val sensorBoundingBox= Cube().apply{
            //translate(0.5f, 0f, 0.5f)
            scale(0.5f, 1f, 0.5f)
        }
    }
}
