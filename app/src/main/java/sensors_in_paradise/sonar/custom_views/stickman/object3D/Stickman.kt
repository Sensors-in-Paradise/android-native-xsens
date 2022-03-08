package sensors_in_paradise.sonar.custom_views.stickman.object3D

import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3D.LineObject3D

class Stickman: LineObject3D() {
    val leftFoot = Vec4(-0.5f, -0.5f, 0f)
    val rightFoot = Vec4(0.5f, -0.5f, 0f)
    val center = Vec4(0f, 0f, 0f)
    val leftWrist = Vec4(-0.5f, +0.5f, 0f)
    val rightWrist = Vec4(0.5f, +0.5f, 0f)

    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>>{
        return arrayOf(Pair(leftFoot, center),Pair(rightFoot, center),Pair(leftWrist, center),Pair(rightWrist, center))
    }
}