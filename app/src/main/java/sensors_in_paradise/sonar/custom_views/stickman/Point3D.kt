package sensors_in_paradise.sonar.custom_views.stickman

import java.lang.IndexOutOfBoundsException

class Point3D(var x: Float, var y: Float, var z: Float, var w: Float) {
    constructor(point3D: Point3D) : this(point3D.x, point3D.y, point3D.z, point3D.w)
    constructor(x: Float, y: Float, z: Float) : this(x, y, z, 1f)
    constructor() : this(0f, 0f, 0f, 1f)

    override operator fun equals(a: Any?): Boolean{
        if (a is Point3D){
            return a.x == x && a.y == y && a.z == z && a.w == w
        }
        return false
    }

    operator fun plusAssign(a: Float) {
        x += a
        y += a
        z += a
    }
    operator fun plusAssign(a: Point3D) {
        x += a.x
        y += a.y
        z += a.z
    }

    operator fun plus(a: Float): Point3D {
        val res = clone()
        res +=a
        return res
    }
    operator fun plus(a: Point3D): Point3D {
        val res = clone()
        res +=a
        return res
    }
    operator fun unaryMinus(): Point3D {
        val res = clone()
        res.x = -x
        res.y = -y
        res.z = -z
        return res
    }
    operator fun minusAssign(a: Float) {
        this += -a
    }
    operator fun minusAssign(a: Point3D) {
        this += -a
    }
    operator fun minus(a: Point3D): Point3D {
        val res = clone()
        res -= a
        return res
    }
    operator fun minus(a: Float): Point3D {
        val res = clone()
        res -= a
        return res
    }
    operator fun timesAssign(a: Float) {
        x *= a
        y *= a
        z *= a
    }
    operator fun timesAssign(a: Point3D) {
        x *= a.x
        y *= a.y
        z *= a.z
    }
    operator fun times(a: Float): Point3D {
        val res = clone()
        res *= a
        return res
    }
    operator fun times(a: Point3D): Point3D {
        val res = clone()
        res *= a
        return res
    }
    operator fun divAssign(a: Float) {
        x /= a
        y /= a
        z /= a
    }
    operator fun divAssign(a: Point3D) {
            x /= a.x
            y /= a.y
            z /= a.z
    }
    operator fun div(a: Point3D): Point3D {
        val res = clone()
        res/=a
        return res
    }
    operator fun div(a: Float): Point3D {
        val res = clone()
        res/=a
        return res
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