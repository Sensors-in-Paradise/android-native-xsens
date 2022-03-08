package sensors_in_paradise.sonar.custom_views.stickman.object3D

import android.graphics.Canvas
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec2
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class LineObject3D: Object3D() {
    private val frustumOpeningAngleW = Math.PI/2f
    private val frustumOpeningAngleH = Math.PI/2f
    //private val s_xy = Matrix4x4(arrayOf(Math.))

    abstract fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>>

    override fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> Vec2) {

    }
}