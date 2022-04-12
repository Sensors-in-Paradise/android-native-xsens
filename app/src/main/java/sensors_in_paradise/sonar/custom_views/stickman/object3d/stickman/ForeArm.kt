package sensors_in_paradise.sonar.custom_views.stickman.object3d.stickman

import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3d.LineObject3D

class ForeArm(wrist: Vec4, elbow: Vec4) : LineObject3D(arrayOf(wrist, elbow), center = elbow.xyz) {
    private val linesToDraw = arrayOf(Pair(wrist, elbow))
    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return linesToDraw
    }
}
