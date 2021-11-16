package sensors_in_paradise.sonar.page1

import com.xsens.dot.android.sdk.models.XsensDotDevice

interface UIDeviceConnectionInterface {
    fun onConnectionUpdateRequested(device: XsensDotDevice, wantsConnection: Boolean)
}
