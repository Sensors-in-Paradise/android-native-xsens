package sensors_in_paradise.sonar.custom_views.stickman

import android.graphics.Color
import android.graphics.Paint
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class Plane: LineObject3D() {
    private val linePaint = Paint(0).apply {
        color = Color.LTGRAY
        strokeWidth=3f
    }
    private val vectorPaint = Paint(0).apply {
        color = Color.WHITE
        strokeWidth=5f
    }
    // lbb for left-bottom-back
    private val lbb = Vec4(-1f, 0f, 1f)
    private val lbf = Vec4(-1f, 0f, -1f)
    private val rbb = Vec4(1f, 0f, 1f)
    private val rbf = Vec4(1f, 0f, -1f)


    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return arrayOf(
            Pair(lbb, lbf),Pair(lbf, rbf),Pair(rbf, rbb),Pair(rbb, lbb))
    }


    override fun getLinePaint(): Paint {
        return linePaint
    }

    override fun getVectorPaint(): Paint {
        return vectorPaint
    }

}