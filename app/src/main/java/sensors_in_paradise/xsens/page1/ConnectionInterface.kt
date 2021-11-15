package sensors_in_paradise.xsens.page1

import com.xsens.dot.android.sdk.models.XsensDotDevice

interface ConnectionInterface {
    fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean)
}