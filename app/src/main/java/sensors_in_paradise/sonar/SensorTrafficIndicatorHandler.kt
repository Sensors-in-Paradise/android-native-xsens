package sensors_in_paradise.sonar

import android.app.Activity
import android.util.Log
import com.xsens.dot.android.sdk.events.XsensDotData
import sensors_in_paradise.sonar.custom_views.SensorDataTrafficIndicatorView
import sensors_in_paradise.sonar.page1.ConnectionInterface

class SensorTrafficIndicatorHandler(
    val activity: Activity,
    private val scannedDevices: XSENSArrayList,
    private val indicator: SensorDataTrafficIndicatorView
) : ConnectionInterface {

    private var connectedSensorAddressIndexMap = mutableMapOf<String, Int>()
    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        connectedSensorAddressIndexMap.clear()
        val connectedDevices = scannedDevices.getConnectedWithOfflineMetadata()
        val tags = Array(connectedDevices.size) { i -> connectedDevices[i].tag }
        activity.runOnUiThread { indicator.setNumSensors(tags) }

        for ((i, device) in connectedDevices.withIndex()) {
            connectedSensorAddressIndexMap[device.address] = i
        }
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        val index = connectedSensorAddressIndexMap[deviceAddress]
        if (index != null) {
            activity.runOnUiThread { indicator.setSensorDataReceived(index) }
        } else {
            Log.e(
                "SensorTrafficIndicatorHandler",
                "Received data from sensor that is not registered as connected device"
            )
        }
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {}
}
