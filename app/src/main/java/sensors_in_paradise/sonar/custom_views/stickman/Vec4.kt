package sensors_in_paradise.sonar.custom_views.stickman

import java.lang.IndexOutOfBoundsException

class Vec4( x: Float,  y: Float,  z: Float,  w: Float): Vec3(arrayOf(x,y,z,w)) {
    constructor(vec4: Vec4) : this(vec4.x, vec4.y, vec4.z, vec4.w)
    constructor(x: Float, y: Float, z: Float) : this(x, y, z, 1f)
    constructor() : this(0f, 0f, 0f, 1f)
    var x: Float
        get() = this[0]
        set(value) {
            this[0]=value
        }
    var y: Float
        get() = this[1]
        set(value) {
            this[1]=value
        }
    var z: Float
        get() = this[2]
        set(value) {
            this[2]=value
        }
    var w: Float
        get() = this[3]
        set(value) {
            this[3]=value
        }


    override fun clone(): Vec4 {
        return Vec4(this)
    }

}