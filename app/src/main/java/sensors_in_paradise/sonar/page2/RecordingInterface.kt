package sensors_in_paradise.sonar.page2

import com.xsens.dot.android.sdk.models.XsensDotRecordingState

interface RecordingInterface {
    fun requestFlashInfo() {
    }
    fun canStartRecording(address: String?, b: Boolean) {
    }
    fun recordingStarted(
        address: String?,
        recordingId: Int,
        success: Boolean,
        xSensDotRecordingState: XsensDotRecordingState?
    ) {
    }
    fun recordingStopped(
        address: String?,
        recordingId: Int,
        success: Boolean,
        xSensDotRecordingState: XsensDotRecordingState?
    ) {
    }
}
