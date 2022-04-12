package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec3
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class LineObject3D(
    vertices: Array<Vec4>,
    children: ArrayList<Object3D> = ArrayList(),
    onObjectChanged: OnObjectChangedInterface? = null,
    center: Vec3 = Vec3(0f, 0f, 0f)
) : Object3D(vertices, children, onObjectChanged, center) {

    open fun getVectorPaint(): Paint {
        return defaultVectorPaint
    }
    abstract fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>>
    open fun getLinePaint(lineIndex: Int? = null): Paint {
        return defaultLinePaint
    }

    override fun drawSelf(
        canvas: Canvas,
        projectedPointToScreen: (p: Vec4) -> PointF,
        projectionMatrix: Matrix4x4
    ) {
        val vectorPaint = getVectorPaint()
        canvas.apply {
            var i = 0
            get3DLinesToDraw().forEach {
                val linePaint = getLinePaint(i)
                val p1 = projectedPointToScreen(projectionMatrix * it.first)
                val p2 = projectedPointToScreen(projectionMatrix * it.second)
                drawLine(p1.x, p1.y, p2.x, p2.y, linePaint)
                drawCircle(p1.x, p1.y, 4f, vectorPaint)
                i++
            }
        }
    }
    companion object {
        private val defaultLinePaint = Paint(0).apply {
            color = Color.BLUE
            strokeWidth = 6f
        }
        private val defaultVectorPaint = Paint(0).apply {
            color = Color.WHITE
            strokeWidth = 9f
        }
    }
}
