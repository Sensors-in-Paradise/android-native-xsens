package sensors_in_paradise.sonar

import com.xsens.dot.android.sdk.events.XsensDotData
import org.junit.Assert
import org.junit.Test
import sensors_in_paradise.sonar.screen_prediction.InMemoryWindow
import kotlin.time.measureTime

class InMemoryWindowTest {
    @Test
    fun initializationTest(){
        val features = arrayOf("Quat_Z_LF","dq_W_LF","dv[1]_LF").map { it.uppercase() }.toTypedArray()
        val window = InMemoryWindow(features, 2)
        assert(features.contentEquals(window.keys.toTypedArray()))
    }
    @Test
    fun addDataTest(){
        val features = arrayOf("Quat_Z_LF","dq_W_RW","dv[1]_LF")
        val window = InMemoryWindow(features, 2)

        val data = XsensDotData().apply {
            quat = floatArrayOf(0f,0f,1f,0f)
            dq = doubleArrayOf(0.0,0.0,0.2,23.0)
            dv = doubleArrayOf(23.0, 0.0, 0.0)
        }

        window.appendSensorData("LF", data)
        Assert.assertFalse(window.hasEnoughDataToCompileWindow())
        window.appendSensorData("RW", data)
        Assert.assertFalse(window.hasEnoughDataToCompileWindow())
        data.sampleTimeFine += 1L

        window.appendSensorData("LF", data)
        Assert.assertFalse(window.hasEnoughDataToCompileWindow())
        data.sampleTimeFine += 1L

        window.appendSensorData("RW", data)
        Assert.assertTrue(window.hasEnoughDataToCompileWindow())

        window.compileWindow()

    }
    @Test
    fun forwardFillDataTest(){
        val features = arrayOf("Quat_Z_LF","dq_W_RW","dv[1]_LF")
        val window = InMemoryWindow(features, 2)

        val data = XsensDotData().apply {
            sampleTimeFine = 0L
            quat = floatArrayOf(0f,Float.NaN,1f,0f)
            dq = doubleArrayOf(0.0,0.0,0.2,23.0)
            dv = doubleArrayOf(23.0, 0.0, Double.NaN)
        }

        window.appendSensorData("LF", data)
        Assert.assertFalse(windowHasNan(window))
    }

    private fun windowHasNan(window: InMemoryWindow): Boolean {
        return window.values.any { arrayList -> arrayList.any { it.second.isNaN() } }
    }
}