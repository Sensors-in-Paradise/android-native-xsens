package sensors_in_paradise.sonar

import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import sensors_in_paradise.sonar.custom_views.stickman.Point3D

class Point3DTest {
    @Before
    fun init() {
    }
    @Test
    fun equalityTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(1f, 0.5f, 0f)
        val p3 = Point3D(2f, 0.5f, 0f)

        assert(p1==p2)
        assert(p2!= p3)
        assert(p1!= p3)
    }
    @Test
    fun scalarPlusAssignTest() {
       val p1 = Point3D(1f, 0.5f, 0f)
        p1 += 2f
        assertEquals(3f, p1.x)
        assertEquals(2.5f, p1.y)
        assertEquals(2f, p1.z)
        assertEquals(1f, p1.w)
    }
    @Test
    fun point3DPlusAssignTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(0f, 2.5f, 1f)
        p1 += p2
        assertEquals(1f, p1.x)
        assertEquals(3f, p1.y)
        assertEquals(1f, p1.z)
        assertEquals(1f, p1.w)
    }
    @Test
    fun scalarMinusAssignTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        p1 -= 2f
        assertEquals(-1f, p1.x)
        assertEquals(-1.5f, p1.y)
        assertEquals(-2f, p1.z)
        assertEquals(1f, p1.w)
    }
    @Test
    fun point3DMinusAssignTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(0f, 2.5f, 1f)
        p1 -= p2
        assertEquals(1f, p1.x)
        assertEquals(-2f, p1.y)
        assertEquals(-1f, p1.z)
        assertEquals(1f, p1.w)
    }
    @Test
    fun scalarTimesAssignTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        p1 *= 2f
        assertEquals(2f, p1.x)
        assertEquals(1f, p1.y)
        assertEquals(0f, p1.z)
        assertEquals(1f, p1.w)
    }
    @Test
    fun point3DTimesAssignTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(0f, 2.5f, 1f)
        p1 *= p2
        assertEquals(0f, p1.x)
        assertEquals(1.25f, p1.y)
        assertEquals(0f, p1.z)
        assertEquals(1f, p1.w)
    }
    @Test
    fun scalarDivAssignTest() {
        val p1 = Point3D(1f, 12f, 0f)
        p1 /= 2f
        assertEquals(0.5f, p1.x)
        assertEquals(6f, p1.y)
        assertEquals(0f, p1.z)
        assertEquals(1f, p1.w)
    }
    @Test
    fun point3DDivAssignTest() {
        val p1 = Point3D(1f, 24f, 6f)
        val p2 = Point3D(1f, 2f, 3f)
        p1/=p2
        assertEquals(1f, p1.x)
        assertEquals(12f, p1.y)
        assertEquals(2f, p1.z)
        assertEquals(1f, p1.w)
    }

    @Test
    fun scalarPlusTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = p1 + 2f
        assertEquals(3f, p2.x)
        assertEquals(2.5f, p2.y)
        assertEquals(2f, p2.z)
        assertEquals(1f, p2.w)
    }
    @Test
    fun point3DPlusTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(0f, 2.5f, 1f)
        val p3 = p1 + p2
        assertEquals(1f, p3.x)
        assertEquals(3f, p3.y)
        assertEquals(1f, p3.z)
        assertEquals(1f, p3.w)
    }
    @Test
    fun scalarMinusTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = p1 - 2f
        assertEquals(-1f, p2.x)
        assertEquals(-1.5f, p2.y)
        assertEquals(-2f, p2.z)
        assertEquals(1f, p2.w)
    }
    @Test
    fun point3DMinusTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(0f, 2.5f, 1f)
        val p3 = p1 - p2
        assertEquals(1f, p3.x)
        assertEquals(-2f, p3.y)
        assertEquals(-1f, p3.z)
        assertEquals(1f, p3.w)
    }
    @Test
    fun scalarTimesTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = p1 * 2f
        assertEquals(2f, p2.x)
        assertEquals(1f, p2.y)
        assertEquals(0f, p2.z)
        assertEquals(1f, p2.w)
    }
    @Test
    fun point3DTimesTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(0f, 2.5f, 1f)
        val p3 = p1 * p2
        assertEquals(0f, p3.x)
        assertEquals(1.25f, p3.y)
        assertEquals(0f, p3.z)
        assertEquals(1f, p3.w)
    }
    @Test
    fun scalarDivTest() {
        val p1 = Point3D(1f, 12f, 0f)
        val p2 = p1 / 2f
        assertEquals(0.5f, p2.x)
        assertEquals(6f, p2.y)
        assertEquals(0f, p2.z)
        assertEquals(1f, p2.w)
    }
    @Test
    fun point3DDivTest() {
        val p1 = Point3D(1f, 24f, 6f)
        val p2 = Point3D(1f, 2f, 3f)
        val p3 = p1/p2
        assertEquals(1f, p3.x)
        assertEquals(12f, p3.y)
        assertEquals(2f, p3.z)
        assertEquals(1f, p3.w)
    }

    @Test
    fun cloneTest() {
        val p1 = Point3D(1f, 0.5f, 0f, 23f)
        val p2 = p1.clone()
        assertEquals(p1.x, p2.x)
        assertEquals(p1.y, p2.y)
        assertEquals(p1.z, p2.z)
        assertEquals(p1.w, p2.w)

        p2 +=0.5f
        Assert.assertNotEquals(p1.x, p2.x)
        Assert.assertNotEquals(p1.y, p2.y)
        Assert.assertNotEquals(p1.z, p2.z)
    }
    @Test
    fun accessorTest() {
        val p1 = Point3D(1f, 0.5f, 0f, 23f)

        assertEquals(p1.x, p1[0])
        assertEquals(p1.y, p1[1])
        assertEquals(p1.z, p1[2])
        assertEquals(p1.w, p1[3])
    }
    @Test
    fun divideByZeroScalarTest() {
        val p1 = Point3D(1f, 0.5f, 0f, 23f)

        assertEquals(p1.x, p1[0])
        assertEquals(p1.y, p1[1])
        assertEquals(p1.z, p1[2])
        assertEquals(p1.w, p1[3])
    }
    @Test
    fun divideByZeroPoint3DTest() {
        val p1 = Point3D(1f, 0.5f, 2f, 23f)
        val p2 = Point3D(0f, 0f, 0f)

        p1 /= p2
        assertEquals(Float.POSITIVE_INFINITY,p1.x)
        assertEquals(Float.POSITIVE_INFINITY,p1.y)
        assertEquals(Float.POSITIVE_INFINITY,p1.z)
        assertEquals( p1[3],p1.w)
    }
    @After
    fun cleanUp() {

    }
}