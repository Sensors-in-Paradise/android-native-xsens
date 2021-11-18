package sensors_in_paradise.sonar.page1

import com.xsens.dot.android.sdk.events.XsensDotData

interface ConnectionInterface {
    fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean)
    fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData)
    fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int)
}
