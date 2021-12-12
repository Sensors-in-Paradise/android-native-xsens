package sensors_in_paradise.sonar.page2

import android.util.Log
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotRecordingCallback
import com.xsens.dot.android.sdk.models.XsensDotRecordingFileInfo
import com.xsens.dot.android.sdk.models.XsensDotRecordingState
import com.xsens.dot.android.sdk.recording.XsensDotRecordingManager
import java.util.*

class RecordingHandler(private val callback: RecordingInterface) : XsensDotRecordingCallback {
    override fun onXsensDotRecordingNotification(address: String?, isEnabled: Boolean) {
        if (isEnabled) {
            callback.requestFlashInfo()
        }
    }

    override fun onXsensDotEraseDone(p0: String?, p1: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotRequestFlashInfoDone(address: String?, usedFlashSpace: Int, totalFlashSpace: Int) {
        Log.d("Request flash info done with: ",
            "usedFlashSpace: $usedFlashSpace and totalFlashSpace:  $totalFlashSpace")
        callback.canStartRecording(address, (1 - (totalFlashSpace / usedFlashSpace) > 0.10))
    }

    override fun onXsensDotRecordingAck(
        address: String?,
        recordingId: Int,
        isSuccess: Boolean,
        XSensDotRecordingState: XsensDotRecordingState?
    ) {
        // can get other recording states here
        if (recordingId ==
            XsensDotRecordingManager.RECORDING_ID_START_RECORDING) {
            // start recording result, check recordingState, it should be success or fail.
            callback.recordingStarted(
                address,
                recordingId,
                isSuccess,
                XSensDotRecordingState
            )
        } else if (recordingId ==
            XsensDotRecordingManager.RECORDING_ID_STOP_RECORDING) {
            // stop recording result, check recordingState, it should be success or fail.
            callback.recordingStopped(
                address,
                recordingId,
                isSuccess,
                XSensDotRecordingState
            )
        }
    }

    override fun onXsensDotGetRecordingTime(p0: String?, p1: Int, p2: Int, p3: Int) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotRequestFileInfoDone(
        address: String?,
        list: ArrayList<XsensDotRecordingFileInfo>?,
        isSuccess: Boolean
    ) {
        if (isSuccess) callback.canExport(address, list) else callback.cantExport(address, list)
    }

    override fun onXsensDotDataExported(
        p0: String?,
        p1: XsensDotRecordingFileInfo?,
        p2: XsensDotData?
    ) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotDataExported(p0: String?, p1: XsensDotRecordingFileInfo?) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotAllDataExported(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotStopExportingData(p0: String?) {
        TODO("Not yet implemented")
    }
}
