package sensors_in_paradise.sonar

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Color
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
        val shouldUseOfflineTag =
            ((super.getTag() == "" || super.getTag() == defaultTag) && _tag != null)
        return if (shouldUseOfflineTag) _tag!! else super.getTag()
    }

    fun getSetColor(): Int {
        return Color.parseColor(
            when (GlobalValues.extractDeviceSetKeyFromTag(tag)) {
                "1" -> "#ff9e80"
                "2" -> "#b9f6ca"
                "3" -> "#ea80fc"
                else -> "#263238"
            }
        )
    }

    fun hasSetColor(): Boolean {
        return when (GlobalValues.extractDeviceSetKeyFromTag(tag)) {
            "1", "2", "3" -> true
            else -> false
        }
    }
}
