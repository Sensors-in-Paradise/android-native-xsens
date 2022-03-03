package sensors_in_paradise.sonar

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import sensors_in_paradise.sonar.custom_views.stickman.Point3D

class Point3DTest {
    @Before
    fun init() {
    }
    @Test
    fun scalarAdditionTest() {
       val p1 = Point3D(1f, 0.5f, 0f)
       p1.add(2f)
        Assert.assertEquals(3f, p1.x)
        Assert.assertEquals(2.5f, p1.y)
        Assert.assertEquals(2f, p1.z)
        Assert.assertEquals(1f, p1.w)
    }
    @Test
    fun point3DAdditionTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        val p2 = Point3D(0f, 2.5f, 1f)
        p1.add(p2)
        Assert.assertEquals(1f, p1.x)
        Assert.assertEquals(3f, p1.y)
        Assert.assertEquals(1f, p1.z)
        Assert.assertEquals(1f, p1.w)
    }
    @Test
    fun scalarMultiplicationTest() {
        val p1 = Point3D(1f, 0.5f, 0f)
        p1.mult(2f)
        Assert.assertEquals(2f, p1.x)
        Assert.assertEquals(1f, p1.y)
        Assert.assertEquals(0f, p1.z)
        Assert.assertEquals(1f, p1.w)
    }
    @Test
    fun cloneTest() {
        val p1 = Point3D(1f, 0.5f, 0f, 23f)
        val p2 = p1.clone()
        Assert.assertEquals(p1.x, p2.x)
        Assert.assertEquals(p1.y, p2.y)
        Assert.assertEquals(p1.z, p2.z)
        Assert.assertEquals(p1.w, p2.w)

        p2.add(0.5f)
        Assert.assertNotEquals(p1.x, p2.x)
        Assert.assertNotEquals(p1.y, p2.y)
        Assert.assertNotEquals(p1.z, p2.z)
    }
    @Test
    fun accessorTest() {
        val p1 = Point3D(1f, 0.5f, 0f, 23f)

        Assert.assertEquals(p1.x, p1[0])
        Assert.assertEquals(p1.y, p1[1])
        Assert.assertEquals(p1.z, p1[2])
        Assert.assertEquals(p1.w, p1[3])
    }
    @After
    fun cleanUp() {

    }
}