package sensors_in_paradise.sonar

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.annotation.UiThread
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.utils.XsensDotParser
import sensors_in_paradise.sonar.custom_views.SensorDataTrafficIndicatorView
import sensors_in_paradise.sonar.custom_views.stickman.Render3DView
import sensors_in_paradise.sonar.custom_views.stickman.object3d.CoordinateSystem3D
import sensors_in_paradise.sonar.page1.ConnectionInterface

class SensorTrafficVisualizationHandler(
    val activity: Activity,
    private val scannedDevices: XSENSArrayList,
    private val indicator: SensorDataTrafficIndicatorView,
    private val orientationRenderViewsLL: LinearLayout
) : ConnectionInterface {
    private val context: Context = activity
    private var connectedSensorAddressIndexMap = mutableMapOf<String, Int>()
    private val orientationRenderViews = ArrayList<Render3DView>()
    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        connectedSensorAddressIndexMap.clear()
        val connectedDevices = scannedDevices.getConnectedWithOfflineMetadata()
        val tags = Array(connectedDevices.size) { i -> connectedDevices[i].tag }
        activity.runOnUiThread {
            indicator.setNumSensors(tags)
        }

        for ((i, device) in connectedDevices.withIndex()) {
            connectedSensorAddressIndexMap[device.address] = i
        }
        activity.runOnUiThread {
            initializeOrientationRenderViews(connectedDevices)
        }
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        val index = connectedSensorAddressIndexMap[deviceAddress]
        if (index != null) {
            val eulerAngles = XsensDotParser.quaternion2Euler(xsensDotData.quat)

            activity.runOnUiThread { indicator.setSensorDataReceived(index)
                orientationRenderViews[index].objects3DToDraw[0].apply {
                    resetToDefaultState()
                    rotate(eulerAngles[1].toFloat(), eulerAngles[2].toFloat(), eulerAngles[0].toFloat())
                }
            }
        } else {
            Log.e(
                "SensorTrafficIndicatorHandler",
                "Received data from sensor that is not registered as connected device"
            )
        }
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {}

    @UiThread
    fun initializeOrientationRenderViews(connectedDevices: XSENSArrayList) {
        orientationRenderViewsLL.removeAllViews()
        orientationRenderViews.clear()

        for (i in 0 until connectedDevices.size) {
            val renderView = Render3DView(context)
            renderView.enableYRotation = true
            renderView.showFPS = true
            renderView.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT, 1.0f)
            renderView.addObject3D(CoordinateSystem3D())
            orientationRenderViewsLL.addView(renderView)
            orientationRenderViews.add(renderView)
        }
    }
}
