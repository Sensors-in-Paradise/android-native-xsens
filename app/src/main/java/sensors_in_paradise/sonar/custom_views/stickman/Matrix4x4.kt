package sensors_in_paradise.sonar.custom_views.stickman

class Matrix4x4(val row1: Array<Float>, val row2: Array<Float>, val row3: Array<Float>, val row4: Array<Float>) {
    constructor() : this(arrayOf(1f,0f,0f,0f),arrayOf(0f,1f,0f,0f),arrayOf(0f,0f,1f,0f),arrayOf(0f,0f,0f,1f))

    fun transform(p: Point3D): Point3D{
        return Point3D(applyRowOnVector(row1, p), applyRowOnVector(row1, p),applyRowOnVector(row1, p),applyRowOnVector(row1, p))
    }
    private fun applyRowOnVector(row: Array<Float>, vector: Point3D): Float{
        assert(row.size == 4)
        var sum = 0f
        for((i, f) in row.withIndex()){
            sum += f * vector[i]
        }
        return sum
    }
}