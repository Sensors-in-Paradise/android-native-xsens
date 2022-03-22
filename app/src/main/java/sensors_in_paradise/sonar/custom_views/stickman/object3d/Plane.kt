package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Color
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

class Plane : TriangleObject3D(arrayOf(lbb, lbf, rbb, rbf), Color.LTGRAY) {

    // lbb for left-bottom-back

    private val trianglesToDraw = arrayOf(Triple(lbb, lbf, rbf), Triple(lbb, rbf, rbb))

    override fun get3DTrianglesToDraw(): Array<Triple<Vec4, Vec4, Vec4>> {
       return trianglesToDraw
    }

    override fun hasChanged(): Boolean {
       return false
    }

    override fun onDrawn() {}

    companion object {
        private val lbb = Vec4(-1f, 0f, 1f)
        private val lbf = Vec4(-1f, 0f, -1f)
        private val rbb = Vec4(1f, 0f, 1f)
        private val rbf = Vec4(1f, 0f, -1f)
    }
}
