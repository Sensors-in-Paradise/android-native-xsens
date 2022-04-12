package sensors_in_paradise.sonar.custom_views.stickman.object3d.stickman

import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3d.LineObject3D

class Extremity(wrist: Vec4, joint: Vec4, shoulder: Vec4) :
    LineObject3D(arrayOf(shoulder), center = joint.xyz) {
    private val foreArm = ForeArm(wrist, joint)
    init {
        children.add(foreArm)
    }

    private val linesToDraw = arrayOf(Pair(joint, shoulder))
    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return linesToDraw
    }

    fun setForearmRotation(xDegrees: Float, yDegrees: Float, zDegrees: Float) {
        foreArm.resetToDefaultState()
        foreArm.rotateEuler(xDegrees, yDegrees, zDegrees)
    }
}
