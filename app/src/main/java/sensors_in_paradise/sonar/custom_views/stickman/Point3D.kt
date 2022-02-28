package sensors_in_paradise.sonar.custom_views.stickman

class Point3D(var x: Float, var y:Float, var z:Float, var w: Float): Array<Float>(4, {i -> 0f}) {
    constructor(point3D: Point3D) : this(point3D.x, point3D.y, point3D.z, point3D.w) {}
    constructor(x: Float, y:Float, z:Float) : this(x, y, z, 1f) {}
    fun mult(a: Float): Point3D{
        x *= a
        y *= a
        z *= a
        return this
    }
    fun add(a: Float): Point3D{
        x += a
        y += a
        z += a
        return this
    }
    fun clone(): Point3D{
        return Point3D(this)
    }

}