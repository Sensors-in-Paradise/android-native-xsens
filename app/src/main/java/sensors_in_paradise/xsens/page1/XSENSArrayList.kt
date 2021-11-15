package sensors_in_paradise.xsens.page1

import com.xsens.dot.android.sdk.models.XsensDotDevice

class XSENSArrayList : ArrayList<XsensDotDevice>() {
    fun contains(deviceAddress: String): Boolean {
        var result = false

        for (device in this) {
            if (device.address == deviceAddress) {
                return true
            }
        }
        return false
    }
    fun get(deviceAddress: String): XsensDotDevice? {
        for (device in this) {
            if (device.address == deviceAddress) {
                return device
            }
        }
        return null;
    }
    fun getConnected(): XSENSArrayList{
        val devices = XSENSArrayList()
        for (device in this) {
            if (device.connectionState == XsensDotDevice.CONN_STATE_CONNECTING) {
                devices.add(device)
            }
        }
        return devices
    }
}
