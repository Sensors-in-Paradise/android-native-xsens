package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Color
import android.graphics.Paint
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class Stickman : LineObject3D(
    arrayOf(
        leftFoot.clone(),
        rightFoot.clone(),
        leftKnee.clone(),
        rightKnee.clone(),
        center.clone(),
        backTop.clone(),
        throatTop.clone(),
        leftElbow.clone(),
        rightElbow.clone(),
        leftWrist.clone(),
        rightWrist.clone()
    ), arrayListOf(head)
) {
    private val _leftFoot = vertices[0]
    private val _rightFoot = vertices[1]
    private val _leftKnee = vertices[2]
    private val _rightKnee = vertices[3]
    private val _center = vertices[4]
    private val _backTop = vertices[5]
    private val _leftElbow = vertices[6]
    private val _rightElbow = vertices[7]
    private val _leftWrist = vertices[8]
    private val _rightWrist = vertices[9]
    private val _throatTop = vertices[10]

    private val linesToDraw = arrayOf(
        Pair(_leftFoot, _leftKnee),
        Pair(_rightFoot, _rightKnee),
        Pair(_leftKnee, _center),
        Pair(_rightKnee, _center),
        Pair(_backTop, _center),
        Pair(_leftElbow, _backTop),
        Pair(_rightElbow, _backTop),
        Pair(_leftWrist, _leftElbow),
        Pair(_rightWrist, _rightElbow),
        Pair(_backTop, _throatTop),
    )

    private val linePaint = Paint(0).apply {
        color = Color.BLUE
        strokeWidth = 6f
    }
    private val vectorPaint = Paint(0).apply {
        color = Color.WHITE
        strokeWidth = 9f
    }

    override fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>> {
        return linesToDraw
    }

    override fun getLinePaint(lineIndex: Int?): Paint {
        return linePaint
    }

    override fun getVectorPaint(): Paint {
        return vectorPaint
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
