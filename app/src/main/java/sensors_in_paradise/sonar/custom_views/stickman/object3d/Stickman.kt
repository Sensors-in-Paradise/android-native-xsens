package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Color
import android.graphics.Paint
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class Stickman : LineObject3D() {
    val leftFoot = Vec4(-0.5f, -0.5f, 0f)
    val rightFoot = Vec4(0.5f, -0.5f, 0f)
    val center = Vec4(0f, 0f, 0f)
    val leftWrist = Vec4(-0.5f, +0.5f, 0f)
    val rightWrist = Vec4(0.5f, +0.5f, 0f)

    private val linePaint = Paint(0).apply {
        color = Color.BLUE
        strokeWidth = 3f
    }
    private val vectorPaint = Paint(0).apply {
        color = Color.WHITE
        strokeWidth = 5f
    }

    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return arrayOf(
            Pair(leftFoot, center),
            Pair(rightFoot, center),
            Pair(leftWrist, center),
            Pair(rightWrist, center)
        )
    }

    override fun getLinePaint(): Paint {
        return linePaint
    }

    override fun getVectorPaint(): Paint {
        return vectorPaint
    }
}
