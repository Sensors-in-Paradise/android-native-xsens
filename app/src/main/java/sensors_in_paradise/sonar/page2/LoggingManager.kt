package sensors_in_paradise.sonar.page2

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.XSENSArrayList
import sensors_in_paradise.sonar.XSensDotMetadataStorage
import sensors_in_paradise.sonar.util.PreferencesHelper
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Suppress("LongParameterList")
class LoggingManager(
    val context: Context,
    private val devices: XSENSArrayList,
    private val recordButton: MaterialButton,
    private val timer: Chronometer,
    private val labelTV: TextView,
    private val personTV: TextView,
    activitiesRV: RecyclerView
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
    private var xSenseMetadataStorage: XSensDotMetadataStorage
    private val tempSensorMacMap = mutableMapOf<String, String>()
    private var isRecording = false

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

        recordButton.setOnClickListener {
            toggleRecording()
        }

        xSenseMetadataStorage = XSensDotMetadataStorage(context)
    }

    private fun toggleRecording() {
        if (isRecording) {
            stopLogging()
        } else {
            if (tryPrepareLogging()) {
                onRecordingStarted?.let { it1 -> it1() }
                startLogging()
                if (!isLabelSelected()) {
                    showActivityDialog(cancellable = false, onSelected = { label, openedTimestamp ->
                        labelTV.text = label
                        labels.add(Pair(openedTimestamp, label))
                        activitiesAdapter.notifyItemInserted(labels.size - 1)
                    })
                }
            }
        }
    }

    private fun showActivityDialog(
        onSelected: (value: String, openedTimestamp: Long) -> Unit,
        cancellable: Boolean? = true
    ) {
        val openedTimestamp = System.currentTimeMillis()
//        PersistentStringArrayDialog(
//            context,
//            "Select an activity label",
//            GlobalValues.getActivityLabelsJSONFile(context),
//            defaultItem = GlobalValues.NULL_ACTIVITY,
//            callback = { value -> onSelected(value, openedTimestamp) },
//            cancellable = cancellable ?: true
//        )
        PersistentCategoriesDialog(
            context,
            "Select an activity label",
            GlobalValues.getActivityLabelsJSONFile(context),
            defaultItems = GlobalValues.DEFINED_ACTIVITIES,
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

    private fun getRecordingFileDir(time: LocalDateTime): File {
        val timeStr = time.toInstant(ZoneOffset.UTC).toEpochMilli().toString()

        return GlobalValues.getSensorRecordingsBaseDir(context).resolve(PreferencesHelper.getRecordingsSubDir(context))
            .resolve(timeStr)
    }

    private fun getRecordingFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("$deviceAddress.csv")
    }

    private fun getNewUnlabelledTempFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("${System.currentTimeMillis()}_$deviceAddress.csv")
    }

    private fun tryPrepareLogging(): Boolean {
        if (!enoughDevicesConnected) {
            Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            return false
        }
        val deviceSetKey =
            xSenseMetadataStorage.tryGetDeviceSetKey(devices.getConnectedWithOfflineMetadata())
                ?: return false

        for (tagPrefix in GlobalValues.sensorTagPrefixes) {
            val tag = GlobalValues.formatTag(tagPrefix, deviceSetKey)
            val address = xSenseMetadataStorage.getAddressForTag(tag)
            tempSensorMacMap[address] = tag
        }
        return true
    }

    private fun initializeLabels(startTime: Long) {
        // clear labels because it is being filled when selecting activities, even when not recording
        while (labels.size > 1) {
            labels.removeAt(0)
        }

        if (labels.size == 1) {
            val activity = labels[0].second
            labels[0] = Pair(startTime, activity)
        }
    }

    private fun startLogging() {
        recordingStartTime = System.currentTimeMillis()
        initializeLabels(recordingStartTime)

        recordButton.setIconResource(R.drawable.ic_baseline_stop_24)
        isRecording = true

        timer.base = SystemClock.elapsedRealtime()
        timer.format = "%s"
        timer.start()
        val fileDir = GlobalValues.getSensorRecordingsTempDir(context)
        fileDir.mkdirs()
        val recordingsKey = LocalDateTime.now()
        tempRecordingMap[recordingsKey] = arrayListOf()

        for (device in devices.getConnected()) {
            device.measurementMode = GlobalValues.MEASUREMENT_MODE
            device.startMeasuring()
            val file = getNewUnlabelledTempFile(fileDir, device.address)

            tempRecordingMap[recordingsKey]!!.add(Pair(device.address, file))
            xsLoggers.add(
                XsensDotLogger(
                    this.context,
                    XsensDotLogger.TYPE_CSV,
                    GlobalValues.MEASUREMENT_MODE,
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

    @SuppressLint("NotifyDataSetChanged")
    private fun stopLogging() {
        timer.stop()
        isRecording = false

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
            recordButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
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
            val destFileDir = getRecordingFileDir(timestamp)
            destFileDir.mkdirs()
            for ((deviceAddress, tempFile) in recordingFiles!!) {
                val destFile = getRecordingFile(destFileDir, deviceAddress)
                Files.copy(tempFile.toPath(), FileOutputStream(destFile))
            }
            val metadataStorage =
                RecordingMetadataStorage(destFileDir.resolve(GlobalValues.METADATA_JSON_FILENAME))
            metadataStorage.setData(
                labels,
                recordingStartTime,
                recordingEndTime,
                person,
                tempSensorMacMap
            )

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
