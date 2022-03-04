package sensors_in_paradise.sonar.custom_views.stickman

import java.lang.IndexOutOfBoundsException

class Matrix4x4(private val row1: Array<Float>, private val row2: Array<Float>, private val row3: Array<Float>, private val row4: Array<Float>) {
    constructor() : this(arrayOf(1f,0f,0f,0f),arrayOf(0f,1f,0f,0f),arrayOf(0f,0f,1f,0f),arrayOf(0f,0f,0f,1f))
    operator fun get(row: Int): Array<Float>{
        return when(row){
            0->row1
            1->row2
            2->row3
            3->row4
            else -> throw IndexOutOfBoundsException("Row-Index must be 0 <= index <= 3")
        }
    }
    operator fun get(row: Int, col: Int): Float{
        return this[row][col]
    }

    operator fun set(row: Int, col: Int, value: Float){
        this[row][col] = value
    }

    operator fun times(p: Vec4): Vec4{
        val res = Vec4()
        for(row in 0..3){
            var sum = 0f
            for(col in 0..3){
                sum += this[row][col] * p[col]
            }
            res[row] = sum
        }
        return res
    }
    private fun applyRowOnVector(row: Array<Float>, vector: Vec4): Float{
        assert(row.size == 4)

        var sum = 0f
        for((i, f) in row.withIndex()){
            sum += f * vector[i]
        }
        return sum
    }


}