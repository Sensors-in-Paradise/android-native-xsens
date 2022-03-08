package sensors_in_paradise.sonar.custom_views.stickman.object3D

import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3D.LineObject3D

class Cube: LineObject3D() {
    // lbb for left-bottom-back
    private val lbb = Vec4(-0.5f, 0f, 0.5f)
    val lbf = Vec4(-0.5f, 0f, -0.5f)
    val ltb = Vec4(-0.5f, 1f, 0.5f)
    val ltf = Vec4(-0.5f, 1f, -0.5f)
    val rbb = Vec4(0.5f, 0f, 0.5f)
    val rbf = Vec4(0.5f, 0f, -0.5f)
    val rtb = Vec4(0.5f, 1f, 0.5f)
    val rtf = Vec4(0.5f, 1f, -0.5f)

    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return arrayOf(
            Pair(lbb, lbf),Pair(lbf, rbf),Pair(rbf, rbb),Pair(rbb, lbb),
            Pair(ltb, ltf),Pair(ltf, rtf),Pair(rbf, rtb),Pair(rtb, ltb),
            Pair(lbb, ltb),Pair(lbf, ltf),Pair(rbf, rtf),Pair(rbb, rtb))
    }

}