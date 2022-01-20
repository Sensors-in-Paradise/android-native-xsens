package sensors_in_paradise.sonar

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.xsens.dot.android.sdk.interfaces.XsensDotDeviceCallback
import com.xsens.dot.android.sdk.models.XsensDotDevice

class XSensDotDeviceWithOfflineMetadata(
    context: Context?,
    device: BluetoothDevice?,
    cb: XsensDotDeviceCallback?,
    private val _tag: String?
) : XsensDotDevice(context, device, cb) {
    private val defaultTag = "Xsens DOT"

    override fun getTag(): String {
        return if ((super.getTag() == "" || super.getTag() == defaultTag) && _tag != null) _tag else super.getTag()
    }
}
