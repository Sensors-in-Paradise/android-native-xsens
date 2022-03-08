package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Color
import android.graphics.Paint
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class Cube : LineObject3D() {
    private val linePaint = Paint(0).apply {
        color = Color.BLUE
        strokeWidth = 3f
    }
    private val vectorPaint = Paint(0).apply {
        color = Color.WHITE
        strokeWidth = 5f
    }
    private val scaleMatrix = Matrix4x4().apply { scale(1f, 3f, 1f) }
    // lbb for left-bottom-back
    private val lbb = scaleMatrix * Vec4(-0.5f, 0f, 0.5f)
    private val lbf = scaleMatrix * Vec4(-0.5f, 0f, -0.5f)
    private val ltb = scaleMatrix * Vec4(-0.5f, 1f, 0.5f)
    private val ltf = scaleMatrix * Vec4(-0.5f, 1f, -0.5f)
    private val rbb = scaleMatrix * Vec4(0.5f, 0f, 0.5f)
    private val rbf = scaleMatrix * Vec4(0.5f, 0f, -0.5f)
    private val rtb = scaleMatrix * Vec4(0.5f, 1f, 0.5f)
    private val rtf = scaleMatrix * Vec4(0.5f, 1f, -0.5f)

    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return arrayOf(
            Pair(lbb, lbf), Pair(lbf, rbf), Pair(rbf, rbb), Pair(rbb, lbb),
            Pair(ltb, ltf), Pair(ltf, rtf), Pair(rtf, rtb), Pair(rtb, ltb),
            Pair(lbb, ltb), Pair(lbf, ltf), Pair(rbf, rtf), Pair(rbb, rtb))
    }

    override fun getLinePaint(): Paint {
        return linePaint
    }

    override fun getVectorPaint(): Paint {
        return vectorPaint
    }
}
