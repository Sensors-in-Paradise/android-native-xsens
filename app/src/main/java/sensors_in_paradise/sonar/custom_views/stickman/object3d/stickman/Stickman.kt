package sensors_in_paradise.sonar.custom_views.stickman.object3d.stickman

import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import sensors_in_paradise.sonar.custom_views.stickman.object3d.Cube
import sensors_in_paradise.sonar.custom_views.stickman.object3d.LineObject3D

class Stickman : LineObject3D(
    arrayOf(
        center.clone(),
        backTop.clone(),
        throatTop.clone()
    ), arrayListOf(head)
) {
    private val _leftFoot = leftFoot.clone()
    private val _rightFoot = rightFoot.clone()
    private val _leftKnee = leftKnee.clone()
    private val _rightKnee = rightKnee.clone()
    private val _center = vertices[0]
    private val _backTop = vertices[1]
    private val _leftElbow = leftElbow.clone()
    private val _rightElbow = rightElbow.clone()
    private val _leftWrist = leftWrist.clone()
    private val _rightWrist = rightWrist.clone()
    private val _throatTop = vertices[2]

    val leftLeg = Extremity(_leftFoot, _leftKnee, _center.clone())
    val rightLeg = Extremity(_rightFoot, _rightKnee, _center.clone())
    val leftArm = Extremity(_leftWrist, _leftElbow, _backTop.clone())
    val rightArm = Extremity(_rightWrist, _rightElbow, _backTop.clone())

    init {
        children.apply {
            add(leftLeg)
            add(rightLeg)
            add(leftArm)
            add(rightArm)
        }
    }
    private val linesToDraw = arrayOf(
        Pair(_backTop, _center),
        Pair(_backTop, _throatTop),
    )

    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return linesToDraw
    }

    companion object {
        private val leftFoot = Vec4(-0.4f, 0f, 0f)
        private val rightFoot = Vec4(0.4f, 0f, 0f)
        private val leftKnee = Vec4(-0.3f, 0.25f, 0f)
        private val rightKnee = Vec4(0.3f, 0.25f, 0f)
        private val center = Vec4(0f, 0.5f, 0f)
        private val backTop = Vec4(0f, 1f, 0f)
        private val leftElbow = Vec4(-0.4f, +0.75f, 0f)
        private val rightElbow = Vec4(0.4f, +0.75f, 0f)
        private val leftWrist = Vec4(-0.5f, +0.5f, 0f)
        private val rightWrist = Vec4(0.5f, +0.5f, 0f)
        private val throatTop = Vec4(0f, +1.1f, 0f)
        private val head = Cube().apply {
            scale(0.15f, 0.2f, 0.15f)
            translate(0f, 1.1f, 0f)
        }
    }
}
