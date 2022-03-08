package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Color
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class Plane : TriangleObject3D(Color.LTGRAY) {

    // lbb for left-bottom-back
    private val scaleMatrix = Matrix4x4().apply { scale(1.5f, 1.5f, 1.5f) }
    private val lbb = scaleMatrix * Vec4(-1f, 0f, 1f)
    private val lbf = scaleMatrix * Vec4(-1f, 0f, -1f)
    private val rbb = scaleMatrix * Vec4(1f, 0f, 1f)
    private val rbf = scaleMatrix * Vec4(1f, 0f, -1f)

    private val trianglesToDraw = arrayOf(Triple(lbb, lbf, rbf), Triple(lbb, rbf, rbb))

    override fun get3DTrianglesToDraw(): Array<Triple<Vec4, Vec4, Vec4>> {
       return trianglesToDraw
    }

    override fun hasChanged(): Boolean {
       return false
    }

    override fun onDrawn() {}
}
