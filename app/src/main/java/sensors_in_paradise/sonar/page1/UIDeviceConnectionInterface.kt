package sensors_in_paradise.sonar.page1

import androidx.annotation.AnyThread
import com.xsens.dot.android.sdk.models.XsensDotDevice

interface UIDeviceConnectionInterface {
    @AnyThread
    fun onConnectionUpdateRequested(device: XsensDotDevice, wantsConnection: Boolean)
    fun onConnectionCancelRequested(device: XsensDotDevice)
    var isSyncing: Boolean
}
