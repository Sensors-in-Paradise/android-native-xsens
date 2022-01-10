package sensors_in_paradise.sonar.page2

import android.R.attr.tag
import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotDevice
import com.xsens.dot.android.sdk.models.XsensDotRecordingFileInfo
import com.xsens.dot.android.sdk.models.XsensDotRecordingState
import com.xsens.dot.android.sdk.recording.XsensDotRecordingManager
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.sonar.BuildConfig
import sensors_in_paradise.sonar.GlobalValues
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class RecordingManager(
    val manager: XsensDotRecordingManager,
    val address: String,
    var label: String = "",
    var person: String = "",
    var logger: XsensDotLogger?,
    var recordingAck: Boolean,
    var canRecord: Boolean,
    var dataFormat: ByteArray?,
    var recordingFiles: ArrayList<XsensDotRecordingFileInfo>,
    var hasExported: Boolean = false
) {
    fun isRecording(): Boolean {
        return manager.recordingState == XsensDotRecordingState.onRecording
    }

    fun stopRecording(): Boolean {
        return manager.stopRecording()
    }

    fun recordingAckSuccess() {
        recordingAck = true
    }

    fun recordingAckFail() {
        recordingAck = false
    }

    fun requestFileInfo() {
        manager.requestFileInfo()
    }

    fun addRecordings(list: ArrayList<XsensDotRecordingFileInfo>) {
        list.forEach { file: XsensDotRecordingFileInfo -> recordingFiles.add(file) }
    }

    fun stdDataFormat() {
        val data = ByteArray(3)
        data[0] = XsensDotRecordingManager.RECORDING_DATA_ID_TIMESTAMP
        data[1] = XsensDotRecordingManager.RECORDING_DATA_ID_EULER_ANGLES
        data[2] = XsensDotRecordingManager.RECORDING_DATA_ID_CALIBRATED_ACC
        dataFormat = data
    }


    fun selectExportedData(dataFormat: ByteArray) {
        manager.selectExportedData(dataFormat)
    }

    fun startExporting() {
        manager.startExporting(recordingFiles)
    }

    fun logExport(
        context: Context,
        device: XsensDotDevice,
        fileInfo: XsensDotRecordingFileInfo,
        exportedData: XsensDotData
    ) {
        if (logger == null) {
            val fileDir = GlobalValues.getRecordingFileDir(
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(fileInfo.startRecordingTimestamp),
                    TimeZone.getDefault().toZoneId()
                ), label, person, context
            )
            fileDir.mkdirs()
            val fileName = fileDir.resolve(device.address + ".csv").absolutePath
            logger = XsensDotLogger.createRecordingsLogger(
                context,
                dataFormat,
                fileName,
                tag.toString(),
                device.firmwareVersion,
                BuildConfig.VERSION_NAME,
                System.currentTimeMillis()
            )
        }
        val activity: Activity = context as Activity
        activity.runOnUiThread {
            Toast.makeText(
                context,
                "Logging",
                Toast.LENGTH_SHORT
            ).show()
        }
        logger?.update(exportedData)
    }

    fun isIdle(): Boolean {
        return manager.recordingState == XsensDotRecordingState.idle
    }

    fun dropLogger() {
        logger?.stop()
        logger = null
    }

    fun clear() {
        manager.clear()
    }
}