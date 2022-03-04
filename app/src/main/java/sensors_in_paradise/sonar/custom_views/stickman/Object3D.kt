package sensors_in_paradise.sonar.custom_views.stickman

abstract class Object3D {
    private val frustumOpeningAngleW = Math.PI/2f
    private val frustumOpeningAngleH = Math.PI/2f
    //private val s_xy = Matrix4x4(arrayOf(Math.))

    abstract fun get3DLinesToDraw(): Array<Pair<Vec4, Vec4>>
}