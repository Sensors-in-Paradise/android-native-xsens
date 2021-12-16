package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotRecordingCallback
import com.xsens.dot.android.sdk.models.XsensDotDevice
import com.xsens.dot.android.sdk.models.XsensDotRecordingFileInfo
import com.xsens.dot.android.sdk.models.XsensDotRecordingState
import com.xsens.dot.android.sdk.recording.XsensDotRecordingManager
import sensors_in_paradise.sonar.XSENSArrayList

class RecordingHandler(
    val context: Context,
    private val devices: XSENSArrayList,
    private val callback: RecordingInterface
) : XsensDotRecordingCallback {
    private val xsRecorders: ArrayList<RecordingManager> = ArrayList()
    private lateinit var activity: Activity

    init {
        createLoggers()
        enableNotifications()

    }

    private fun findManager(address: String): RecordingManager? {
        return xsRecorders.find { recorder: RecordingManager -> recorder.address == address }
    }

    fun startRecording() {
        if (xsRecorders.all { manager: RecordingManager -> manager.canRecord }) {
            xsRecorders.forEach { manager: RecordingManager -> manager.manager.startRecording() }
        } else {
            activity.runOnUiThread {
                Toast.makeText(
                    context,
                    "Deleting files on overcrowded Dots",
                    Toast.LENGTH_SHORT
                ).show()
            }
            xsRecorders.filter { manager: RecordingManager -> !manager.canRecord }
                .forEach { it.manager.eraseRecordingData() }
        }
    }

    fun stopRecording() {
        if (xsRecorders.all { manager: RecordingManager -> manager.isRecording() }) {
            xsRecorders.forEach { manager: RecordingManager -> manager.stopRecording() }
        }
    }

    private fun createLoggers() {
        for (device in devices) {
            xsRecorders.add(
                RecordingManager(
                    XsensDotRecordingManager(context, device, this),
                    device.address,
                    label = "",
                    person = "",
                    logger = null,
                    recordingAck = false,
                    canRecord = false,
                    dataFormat = null,
                    ArrayList()
                )
            )
        }
    }

    private fun enableNotifications() {
        for (recorder in xsRecorders) {
            recorder.manager.enableDataRecordingNotification()
        }
    }

    override fun onXsensDotRecordingNotification(address: String, isEnabled: Boolean) {
        if (isEnabled) {
            findManager(address)?.manager?.requestFlashInfo()
        }
    }

    override fun onXsensDotRequestFlashInfoDone(
        address: String?,
        usedFlashSpace: Int,
        totalFlashSpace: Int
    ) {
        if (1 - usedFlashSpace / totalFlashSpace > 0.10) {
            address?.let {
                findManager(it)?.canRecord =
                    true
            }
        }
    }

    override fun onXsensDotRecordingAck(
        address: String?,
        recordingId: Int,
        isSuccess: Boolean,
        recordingState: XsensDotRecordingState?
    ) {
        if (recordingId == XsensDotRecordingManager.RECORDING_ID_START_RECORDING) {
            // start recording result, check recordingState, it should be success or fail. ... }
            if (recordingState == XsensDotRecordingState.success) {
                TODO()
            }
            if (recordingState == XsensDotRecordingState.fail) {
                TODO()
            }
        }
        if (recordingId == XsensDotRecordingManager.RECORDING_ID_STOP_RECORDING) {
            // stop recording result, check recordingState, it should be success or fail. ... }
            TODO()
        }
    }


    override fun onXsensDotGetRecordingTime(
        address: String?,
        startUTCSeconds: Int,
        totalRecordingSeconds: Int,
        remainingRecordingSeconds: Int
    ) {
    }

    private fun requestFileInfo() {
        xsRecorders.forEach { manager: RecordingManager -> manager.requestFileInfo() }
    }

    private fun selectStdExportQuantities() {
        xsRecorders.forEach { manager: RecordingManager ->
            manager.stdDataFormat()
            manager.dataFormat?.let { manager.selectExportedData(it) }
        }
    }

    private fun exportOnAll() {
        if (recordersIdle())
        xsRecorders.forEach { manager: RecordingManager -> manager.startExporting() }
    }

    private fun recordersIdle(): Boolean {
       return xsRecorders.all { manager: RecordingManager -> manager.isIdle() }
    }

    fun startExporting() {
        requestFileInfo()
        selectStdExportQuantities()
        exportOnAll()
    }


    override fun onXsensDotRequestFileInfoDone(
        address: String?,
        list: ArrayList<XsensDotRecordingFileInfo>?,
        isSuccess: Boolean
    ) {
        if (list != null && address != null) {
            findManager(address)?.addRecordings(list)
        } else {
            activity.runOnUiThread {
                Toast.makeText(
                    context,
                    "No recording Files Found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun findDevice(address: String): XsensDotDevice? {
        return devices.find { device: XsensDotDevice -> device.address == address }
    }

    private fun dataExported(
        address: String,
        fileInfo: XsensDotRecordingFileInfo,
        exportedData: XsensDotData
    ) {
        findDevice(address)?.let {
            findManager(address)?.logExport(
                context,
                it,
                fileInfo,
                exportedData
            )
        }
    }


    override fun onXsensDotDataExported(
        address: String?,
        fileInfo: XsensDotRecordingFileInfo,
        exportedData: XsensDotData?
    ) {
        if (address != null && exportedData != null) dataExported(address, fileInfo, exportedData)
    }

    private fun fileExported(
        address: String
    ) {
        val manager = findManager(address)
        if (manager != null) {
            manager.dropLogger()
            manager.hasExported = true
        }
    }

    override fun onXsensDotDataExported(address: String?, fileInfo: XsensDotRecordingFileInfo) {
        if (address != null) fileExported(address)

    }

    override fun onXsensDotAllDataExported(address: String?) {
        if (address != null) fileExported(address)
        if (xsRecorders.all { manager: RecordingManager -> manager.hasExported }) {
            xsRecorders.forEach { manager: RecordingManager -> manager.clear() }
            exportingFinished()
        }
    }

    override fun onXsensDotStopExportingData(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotEraseDone(p0: String?, isSuccess: Boolean) {
        if (isSuccess) activity.runOnUiThread {
            Toast.makeText(
                context,
                "Successfully deleted some Files.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun exportingFinished() {
        callback.exportingFinished()
    }
}