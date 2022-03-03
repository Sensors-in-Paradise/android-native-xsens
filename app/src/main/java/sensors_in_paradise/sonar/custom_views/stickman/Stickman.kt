package sensors_in_paradise.sonar.custom_views.stickman

class Stickman: Object3D() {
    val leftFoot = Point3D(-0.5f, -0.5f, 0f)
    val rightFoot = Point3D(0.5f, -0.5f, 0f)
    val center = Point3D(0f, 0f, 0f)
    val leftWrist = Point3D(-0.5f, +0.5f, 0f)
    val rightWrist = Point3D(0.5f, +0.5f, 0f)

    override fun get3DLinesToDraw(): Array<Pair<Point3D, Point3D>>{
        return arrayOf(Pair(leftFoot, center),Pair(rightFoot, center),Pair(leftWrist, center),Pair(rightWrist, center))
    }
}