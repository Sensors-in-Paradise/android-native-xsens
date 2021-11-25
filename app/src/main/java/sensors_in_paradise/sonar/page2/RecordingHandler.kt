package sensors_in_paradise.sonar.page2

import android.util.Log
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotRecordingCallback
import com.xsens.dot.android.sdk.models.XsensDotRecordingFileInfo
import com.xsens.dot.android.sdk.models.XsensDotRecordingState
import java.util.ArrayList

class RecordingHandler(val callback: RecordingInterface) : XsensDotRecordingCallback {
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
        callback.canStartRecording(address, (1 - totalFlashSpace / usedFlashSpace >= 0.10))
    }

    override fun onXsensDotRecordingAck(
        p0: String?,
        p1: Int,
        p2: Boolean,
        p3: XsensDotRecordingState?
    ) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotGetRecordingTime(p0: String?, p1: Int, p2: Int, p3: Int) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotRequestFileInfoDone(
        p0: String?,
        p1: ArrayList<XsensDotRecordingFileInfo>?,
        p2: Boolean
    ) {
        TODO("Not yet implemented")
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

