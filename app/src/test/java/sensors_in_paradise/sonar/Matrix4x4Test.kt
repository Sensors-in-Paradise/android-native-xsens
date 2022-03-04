package sensors_in_paradise.sonar

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import sensors_in_paradise.sonar.custom_views.stickman.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.Vec4

class Matrix4x4Test {
    @Before
    fun init() {
    }
    @Test
    fun accessRowTest() {
        val m = Matrix4x4()
        val row = m[2]
        assertEquals(0f,row[0])
        assertEquals(0f,row[1])
        assertEquals(1f,row[2])
        assertEquals(0f,row[3])
    }
    @Test
    fun accessScalarTest() {
        val m = Matrix4x4()
        assertEquals(0f,m[2][0])
        assertEquals(0f,m[2][1])
        assertEquals(1f,m[2][2])
        assertEquals(0f,m[2][3])
    }
    @Test
    fun setScalarTest() {
        val m = Matrix4x4()
        m[2][3] = 23f
        assertEquals(23f, m[2][3])
    }
    @Test
    fun transformVectorTest() {
        val p1 = Vec4(2f, 06f, 1f,1f)
        val m = Matrix4x4(arrayOf(1f,2f,1f,1f),arrayOf(0f,1f,0f,1f),arrayOf(2f,3f,4f,1f),arrayOf(1f,1f,1f,1f))
        val res = m *p1
        assertEquals(16f,res.x)
        assertEquals(7f,res.y)
        assertEquals(27f,res.z)
        assertEquals(10f,res.w)
    }
    @Test
    fun transformVectorWithUnitMatrixTest() {
        val m = Matrix4x4()
        val p = Vec4(1f, 0.5f, 2f)
        val res = m * p
        assertEquals(1f,res.x)
        assertEquals(0.5f,res.y)
        assertEquals(2f,res.z)
        assertEquals(1f,res.w)
    }
    @Test
    fun instantiateUnitMatrixTest() {
        val m = Matrix4x4()
        // row 0
        assertEquals(1f,m[0][0])
        assertEquals(0f,m[0][1])
        assertEquals(0f,m[0][2])
        assertEquals(0f,m[0][3])
        //row 1
        assertEquals(0f,m[1][0])
        assertEquals(1f,m[1][1])
        assertEquals(0f,m[1][2])
        assertEquals(0f,m[1][3])
        // row 2
        assertEquals(0f,m[2][0])
        assertEquals(0f,m[2][1])
        assertEquals(1f,m[2][2])
        assertEquals(0f,m[2][3])
        // row 3
        assertEquals(0f,m[3][0])
        assertEquals(0f,m[3][1])
        assertEquals(0f,m[3][2])
        assertEquals(1f,m[3][3])
    }


    @After
    fun cleanUp() {

    }
}