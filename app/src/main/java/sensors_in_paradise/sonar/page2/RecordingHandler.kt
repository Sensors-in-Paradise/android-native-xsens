package sensors_in_paradise.sonar.page2

import android.content.Context
import android.util.Log
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

    init {
        createRecordingManagers()
        enableNotifications()
    }

    private fun findManager(address: String): RecordingManager? {
        return xsRecorders.find { recorder: RecordingManager -> recorder.address == address }
    }

    fun startRecording() {
        if (xsRecorders.all { manager: RecordingManager -> manager.canRecord }) {
            xsRecorders.forEach { manager: RecordingManager ->
                manager.manager.startRecording()
                Log.d("RecordingHandler", "start recording on $manager")
            }
        } else {
            xsRecorders.filter { manager: RecordingManager -> !manager.canRecord }
                .forEach {
                    it.manager.eraseRecordingData()
                    Log.d("RecordingHandler", "deleting data on $it")
                }
        }
    }

    fun stopRecording() {
        xsRecorders.forEach { manager: RecordingManager ->
            manager.stopRecording()
            Log.d("RecordingHandler", "stop recording on $manager")

        }
    }

    private fun createRecordingManagers() {
        for (device in devices) {
            xsRecorders.add(
                RecordingManager(
                    XsensDotRecordingManager(context, device, this),
                    device.address,
                    label = "",
                    person = "",
                    logger = null,
                    recordingAck = false,
                    canRecord = true,
                    dataFormat = null,
                    ArrayList()
                )
            )
            Log.d("RecordingHandler", "adding recorder for $device")
        }
    }

    private fun enableNotifications() {
        for (recorder in xsRecorders) {
            recorder.manager.enableDataRecordingNotification()
            Log.d("RecordingHandler", "enabling data recording notification for $recorder")
        }
    }

    override fun onXsensDotRecordingNotification(address: String, isEnabled: Boolean) {
        if (isEnabled) {
            with(findManager(address), {
                this?.requestFlashInfo()
                Log.d("RecordingHandler", "requesting flash info for $this")
            })
        }
    }

    override fun onXsensDotRequestFlashInfoDone(
        address: String?,
        usedFlashSpace: Int,
        totalFlashSpace: Int
    ) {
        if (usedFlashSpace / totalFlashSpace < .90) {
            address?.let {
                with(findManager(it), {
                    this?.canRecord = true
                    Log.d("RecordingHandler", "can record on $this")
                })
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
                Log.d("RecordingHandler", "successful start recording on ${address?.let { findManager(it) }}")
            }
            if (recordingState == XsensDotRecordingState.fail) {
                Log.d("RecordingHandler", "fail start recording on ${address?.let { findManager(it) }}")
            }
        }
        if (recordingId == XsensDotRecordingManager.RECORDING_ID_STOP_RECORDING) {
            // stop recording result, check recordingState, it should be success or fail. ... }
            Log.d("RecordingHandler", "stop recording on ${address?.let { findManager(it) }} was $isSuccess")
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
        xsRecorders.forEach { manager: RecordingManager ->
            manager.requestFileInfo()
            Log.d("RecordingHandler", "requesting File info on $manager")
        }

    }

    private fun selectStdExportQuantities() {
        xsRecorders.forEach { manager: RecordingManager ->
            manager.stdDataFormat()
            manager.dataFormat?.let { manager.selectExportedData(it) }
            Log.d("RecordingHandler", "std export quantities selected on $manager")
        }
    }

    private fun exportOnAll() {
        xsRecorders.forEach { manager: RecordingManager -> manager.startExporting() }
    }

    private fun recordersIdle(): Boolean {
        return xsRecorders.all { manager: RecordingManager -> manager.isIdle() }
    }

    fun startExporting() {
        Log.d("RecordingHandler", "starting export")
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
            Log.d("RecordingHandler", "RequestFileInfoDone with $list")
            findManager(address)?.addRecordings(list)
        } else {
            Log.d("RecordingHandler", "addRecording failed")
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
            Log.d("RecordingHandler", "Export to $address with $fileInfo")
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
            Log.d("RecordingHandler", "Exported one file on $manager")
            manager.dropLogger()
            manager.hasExported = true
        }
    }

    override fun onXsensDotDataExported(address: String?, fileInfo: XsensDotRecordingFileInfo) {
        if (address != null) fileExported(address)

    }

    override fun onXsensDotAllDataExported(address: String?) {
        if (address != null) fileExported(address)
        Log.d("RecordingHandler", "All Data exported on ${address?.let { findManager(it) }}")
        if (xsRecorders.all { manager: RecordingManager -> manager.hasExported }) {
            xsRecorders.forEach { manager: RecordingManager -> manager.clear() }
            exportingFinished()
        }
    }

    override fun onXsensDotStopExportingData(p0: String?) {
    }

    override fun onXsensDotEraseDone(address: String?, isSuccess: Boolean) {
        if (isSuccess) Log.d(
            "RecordingHandler", "Successfully erased data on ${
                address?.let {
                    findDevice(
                        it
                    )
                }
            }"
        )
    }

    private fun exportingFinished() {
        callback.exportingFinished()
        Log.d("RecordingHandler", "Exported All Files")
    }
}