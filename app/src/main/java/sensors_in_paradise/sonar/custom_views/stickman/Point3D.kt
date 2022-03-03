package sensors_in_paradise.sonar.custom_views.stickman

import java.lang.IndexOutOfBoundsException

class Point3D(var x: Float, var y: Float, var z: Float, var w: Float) {
    constructor(point3D: Point3D) : this(point3D.x, point3D.y, point3D.z, point3D.w) {}
    constructor(x: Float, y: Float, z: Float) : this(x, y, z, 1f) {}

    fun mult(a: Float): Point3D {
        x *= a
        y *= a
        z *= a
        return this
    }

    fun add(a: Float): Point3D {
        x += a
        y += a
        z += a
        return this
    }
    fun add(a: Point3D): Point3D {
        x += a.x
        y += a.y
        z += a.z
        return this
    }
    fun clone(): Point3D {
        return Point3D(this)
    }

    operator fun get(index: Int): Float {
        return when (index) {
            0 -> x
            1 -> y
            2 -> z
            3 -> w
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds. It must be 0 <= index <= 3. Can't access value.")
        }
    }

    operator fun set(index: Int, value: Float) {
        when (index) {
            0 -> x = value
            1 -> y = value
            2 -> z = value
            3 -> w = value
            else -> throw IndexOutOfBoundsException("Index $index is out of bounds. It must be 0 <= index <= 3. Can't set value")
        }
    }
}