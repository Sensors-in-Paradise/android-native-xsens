package sensors_in_paradise.sonar.page2

import android.content.Context
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.Spinner
import com.xsens.dot.android.sdk.models.XsensDotPayload
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.XSENSArrayList
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LoggingManager(
    val context: Context,
    private val devices: XSENSArrayList,
    private val labelsStorage: LabelsStorage,
    private val endButton: Button,
    private val timer: Chronometer,
    private val spinner: Spinner
) {
    val xsLoggers: ArrayList<XsensDotLogger> = ArrayList()

    /** Map of timestamp of recording as key and ArrayList of Pairs of sensor
     * deviceAddress and associated recording file*/
    private val tempRecordingMap: MutableMap<LocalDateTime, ArrayList<Pair<String, File>>> =
        mutableMapOf()
    private var onRecordingDone: ((String, String) -> Unit)? = null
    private fun getRecordingFileDir(time: LocalDateTime, label: String): File {
        val timeStr = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(time)
        return GlobalValues.getSensorRecordingsBaseDir(context).resolve(
            label
        ).resolve(timeStr)
    }

    private fun getRecordingFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("$deviceAddress.csv")
    }

    private fun getNewUnlabelledTempFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("${System.currentTimeMillis()}_$deviceAddress.csv")
    }

    private fun getRecordingName(fileDir: File): String {
        return fileDir.toString()
    }

    fun startLogging() {
        endButton.isEnabled = true

        timer.base = SystemClock.elapsedRealtime()
        timer.format = "%s" // set the format for a chronometer
        timer.start()
        val fileDir = GlobalValues.getSensorRecordingsTempDir(context)
        fileDir.mkdirs()
        // spinner.setSelection(0)
        val recordingsKey = LocalDateTime.now()
        tempRecordingMap[recordingsKey] = arrayListOf()

        for (device in devices.getConnected()) {
            device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
            device.startMeasuring()
            val file = getNewUnlabelledTempFile(fileDir, device.address)

            tempRecordingMap[recordingsKey]!!.add(Pair(device.address, file))
            xsLoggers.add(
                XsensDotLogger(
                    this.context,
                    XsensDotLogger.TYPE_CSV,
                    XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION,
                    file.absolutePath,
                    device.address,
                    "1",
                    false,
                    1,
                    null as String?,
                    "appVersion",
                    0
                )
            )
        }
    }

    fun stopLogging() {
        val isLabelSelected =
            spinner.selectedItemPosition != 0 && spinner.selectedItemPosition != spinner.count - 1
        if (!isLabelSelected) {
            val dialog = PostLabellingDialog(context, labelsStorage.getLabelsArray())
            dialog.setOnLabelSelectedListener { label -> moveTempFiles(label) }
        }
        timer.stop()
        for (logger in xsLoggers) {
            logger.stop()
        }
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }
        if (isLabelSelected) {
            moveTempFiles(spinner.selectedItem.toString())
        }
        spinner.setSelection(0)
        endButton.isEnabled = false
        xsLoggers.clear()
    }

    private fun moveTempFiles(label: String) {
        val keys = tempRecordingMap.keys.asIterable()
        for (timestamp in keys) {
            val recordingFiles = tempRecordingMap[timestamp]
            val destFileDir = getRecordingFileDir(timestamp, label)
            destFileDir.mkdirs()
            for ((deviceAddress, tempFile) in recordingFiles!!) {
                val destFile = getRecordingFile(destFileDir, deviceAddress)
                Files.copy(tempFile.toPath(), FileOutputStream(destFile))
            }
            onRecordingDone?.let { it(getRecordingName(destFileDir), timer.text.toString()) }
        }
        tempRecordingMap.clear()
    }
    fun setOnRecordingDone(onRecordingDone: (String, String) -> Unit) {
        this.onRecordingDone = onRecordingDone
    }
}
