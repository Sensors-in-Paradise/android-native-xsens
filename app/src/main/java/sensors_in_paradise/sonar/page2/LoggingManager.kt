package sensors_in_paradise.sonar.page2

import android.content.Context
import android.os.SystemClock
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
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

@Suppress("LongParameterList")
class LoggingManager(
    val context: Context,
    private val devices: XSENSArrayList,
    private val startButton: Button,
    private val endButton: Button,
    private val timer: Chronometer,
    private val labelTV: TextView,
    private val personTV: TextView,
    private val activitiesRV: RecyclerView
) {

    val xsLoggers: ArrayList<XsensDotLogger> = ArrayList()
    var enoughDevicesConnected = false

    /** Map of timestamp of recording as key and ArrayList of Pairs of sensor
     * deviceAddress and associated recording file*/
    private val tempRecordingMap: MutableMap<LocalDateTime, ArrayList<Pair<String, File>>> =
        mutableMapOf()
    private var onRecordingDone: ((Recording) -> Unit)? = null
    private var onRecordingStarted: (() -> Unit)? = null
    private val labels = ArrayList<Pair<Long, String>>()
    private var recordingStartTime = 0L
    private val activitiesAdapter = ActivitiesAdapter(labels)
    init {
        activitiesRV.adapter = activitiesAdapter

        labelTV.setOnClickListener {
            showActivityDialog({ label, openedTimestamp ->
                labelTV.text = label
                labels.add(Pair(openedTimestamp, label))
                activitiesAdapter.notifyItemInserted(labels.size - 1)
            })
        }
        personTV.setOnClickListener {
            showPersonDialog({ person ->
                personTV.text = person
            })
        }
        endButton.isEnabled = false

        startButton.setOnClickListener {
            if (enoughDevicesConnected) {
                onRecordingStarted?.let { it1 -> it1() }
                startLogging()
                if (!isLabelSelected()) {
                    showActivityDialog(cancellable = false, onSelected = { label, openedTimestamp ->
                        labelTV.text = label
                        labels.add(Pair(openedTimestamp, label))
                        activitiesAdapter.notifyItemInserted(labels.size - 1)
                    })
                }
            } else {
                Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            }
        }
        startButton.isEnabled = true
        endButton.setOnClickListener {
            stopLogging()
        }
    }

    private fun showActivityDialog(
        onSelected: (value: String, openedTimestamp: Long) -> Unit,
        cancellable: Boolean? = true
    ) {
        val openedTimestamp = System.currentTimeMillis()
        PersistentStringArrayDialog(
            context,
            "Select an activity Label",
            GlobalValues.getActivityLabelsJSONFile(context),
            defaultItem = GlobalValues.NULL_ACTIVITY,
            callback = { value -> onSelected(value, openedTimestamp) },
            cancellable = cancellable ?: true
        )
    }

    private fun showPersonDialog(
        onSelected: (value: String) -> Unit,
        cancellable: Boolean? = true
    ) {
        PersistentStringArrayDialog(
            context,
            "Select a Person",
            GlobalValues.getPeopleJSONFile(context),
            defaultItem = GlobalValues.UNKNOWN_PERSON,
            callback = onSelected, cancellable = cancellable ?: true
        )
    }

    private fun getRecordingFileDir(time: LocalDateTime, person: String): File {
        val timeStr = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(time)
        return GlobalValues.getSensorRecordingsBaseDir(context).resolve(person).resolve(timeStr)
    }

    private fun getRecordingFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("$deviceAddress.csv")
    }

    private fun getNewUnlabelledTempFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("${System.currentTimeMillis()}_$deviceAddress.csv")
    }

    private fun startLogging() {
        endButton.isEnabled = true
        startButton.isEnabled = false
        timer.base = SystemClock.elapsedRealtime()
        timer.format = "%s" // set the format for a chronometer
        timer.start()
        val fileDir = GlobalValues.getSensorRecordingsTempDir(context)
        fileDir.mkdirs()
        val recordingsKey = LocalDateTime.now()
        tempRecordingMap[recordingsKey] = arrayListOf()
        recordingStartTime = System.currentTimeMillis()
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

    fun cancelLogging() {
        labels.clear()
    }

    private fun stopLogging() {
        timer.stop()
        val recordingEndTime = System.currentTimeMillis()
        for (logger in xsLoggers) {
            logger.stop()
        }
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }

        resolveMissingFields {
            moveTempFiles(personTV.text.toString(), recordingEndTime)
            labelTV.text = ""
            personTV.text = ""
            endButton.isEnabled = false
            startButton.isEnabled = true
            xsLoggers.clear()
            labels.clear()
            activitiesAdapter.notifyDataSetChanged()
        }
    }

    private fun isLabelSelected(): Boolean {
        val labelText = labelTV.text.toString()
        return labelText != ""
    }

    private fun resolveMissingFields(onAllResolved: () -> Unit) {
        val personText = personTV.text.toString()
        val isLabelSelected =
            isLabelSelected()
        val isPersonSelected =
            personText != ""
        if (!isLabelSelected) {
            showActivityDialog(cancellable = false, onSelected = { label, _ ->
                labelTV.text = label
                resolveMissingFields(onAllResolved)
                labels.add(Pair(recordingStartTime, label))
            })
        } else if (!isPersonSelected) {
            showPersonDialog(cancellable = false, onSelected = { person ->
                personTV.text = person
                resolveMissingFields(onAllResolved)
            })
        } else {
            onAllResolved()
        }
    }

    private fun moveTempFiles(person: String, recordingEndTime: Long) {
        val keys = tempRecordingMap.keys.asIterable()
        for (timestamp in keys) {
            val recordingFiles = tempRecordingMap[timestamp]
            val destFileDir = getRecordingFileDir(timestamp, person)
            destFileDir.mkdirs()
            for ((deviceAddress, tempFile) in recordingFiles!!) {
                val destFile = getRecordingFile(destFileDir, deviceAddress)
                Files.copy(tempFile.toPath(), FileOutputStream(destFile))
            }
            val metadataStorage = RecordingMetadataStorage(destFileDir.resolve(GlobalValues.METADATA_JSON_FILENAME))
            metadataStorage.setData(labels, recordingStartTime, recordingEndTime, person, GlobalValues.sensorTagMap)

            onRecordingDone?.let { it(Recording(destFileDir, metadataStorage)) }
        }
        tempRecordingMap.clear()
    }

    fun setOnRecordingDone(onRecordingDone: (Recording) -> Unit) {
        this.onRecordingDone = onRecordingDone
    }
    fun setOnRecordingStarted(onRecordingStarted: () -> Unit) {
        this.onRecordingStarted = onRecordingStarted
    }
}
