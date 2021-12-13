package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotDevice
import com.xsens.dot.android.sdk.models.XsensDotPayload
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.TextInputDialog.AcceptanceInterface
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class Page2Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton
    private lateinit var xsLoggers: ArrayList<XsensDotLogger>
    private lateinit var uiHelper: UIHelper
    private lateinit var spinner: Spinner
    private lateinit var recyclerViewRecordings: RecyclerView
    private lateinit var activityCountTextView: TextView

    private var fileDirectory: String = ""
    private lateinit var recordingsAdapter: RecordingsAdapter

    private var numConnectedDevices = 0
    private var numDevices = 5

    private lateinit var recordingName: String
    private lateinit var recordingsManager: RecordingDataManager
    private lateinit var labelsStorage: RecordingLabelsStorage
    private lateinit var labelsAdapter: RecordingLabelsAdapter

    private val unlabelledRecordingMap: MutableMap<String, Pair<LocalDateTime, File>> = mutableMapOf()
    override fun activityCreated(activity: Activity) {
        this.context = activity
        this.uiHelper = UIHelper(this.context)
        fileDirectory = this.context.getExternalFilesDir(null).toString()

        timer = activity.findViewById(R.id.timer)
        startButton = activity.findViewById(R.id.buttonStart)
        endButton = activity.findViewById(R.id.buttonEnd)
        spinner = activity.findViewById(R.id.spinner)
        activityCountTextView = activity.findViewById(R.id.tv_activity_counts)

        recordingsManager = RecordingDataManager(fileDirectory, RecordingPreferences(context))
        recyclerViewRecordings = activity.findViewById(R.id.recyclerViewRecordings)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewRecordings.layoutManager = linearLayoutManager
        recordingsAdapter = RecordingsAdapter(recordingsManager)
        recyclerViewRecordings.adapter = recordingsAdapter

        labelsStorage = RecordingLabelsStorage(context)

        labelsAdapter = RecordingLabelsAdapter(activity)
        labelsAdapter.setDeleteButtonClickListener(object : RecordingLabelsAdapter.ClickInterface {
            override fun onDeleteButtonPressed(label: String) {
                ApproveDialog(
                    context, "Do you really want to delete the label $label?"
                ) { p0, p1 ->
                    labelsStorage.removeLabel(label)
                    spinner.setSelection(0)
                    labelsAdapter.remove(label)
                }
            }
        })

        spinner.adapter = labelsAdapter
        labelsAdapter.add("Select label")
        for (s in labelsStorage.getLabelsArray()) {
            labelsAdapter.add(s)
        }
        labelsAdapter.add("Add new label")
        endButton.isEnabled = false

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //startButton.isEnabled = false
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val isAddNewLabel = position == spinner.count - 1
                //startButton.isEnabled = position != 0 && !isAddNewLabel
                if (isAddNewLabel) {
                    handleCreateLabelRequested()
                }
            }
        }

        xsLoggers = ArrayList()
        startButton.isEnabled = true
        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {
                startLogging()
            } else {
                Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            }
        }

        endButton.setOnClickListener {
            stopLogging()
        }

        updateActivityCounts()
    }

    private fun handleCreateLabelRequested() {
        val currentLabels = labelsStorage.getLabelsArray()
        val promptInterface =  {value:String -> labelsStorage.addLabel(value.toLowerCase())
            labelsAdapter.insert(value.toLowerCase(), 1)
            spinner.setSelection(1)}

        val acceptanceInterface = {
                text:String->
            val alreadyAdded = currentLabels.contains(text)
            val valid = text != ""
            if (valid) {
                Pair(
                    !alreadyAdded,
                    if (alreadyAdded) "Label already added" else null
                )
            }else {
                Pair(
                    false,
                    "Invalid label"
                )
            }
        }
        TextInputDialog(
            context, "Add new label",
           promptInterface , "Label", acceptanceInterface = acceptanceInterface
        ).setCancelListener { _ -> spinner.setSelection(0) }
    }

    private fun getRecordingFileDir(time: LocalDateTime, label: String): File {
        val timeStr = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(time)
        return GlobalValues.getSensorDataBaseDir(context).resolve(
            label
        ).resolve(timeStr)
    }

    private fun getRecordingFile(time: LocalDateTime, label: String, deviceAddress: String): File {
        return getRecordingFileDir(time, label).resolve("${deviceAddress}.csv")
    }

    private fun getRecordingFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("${deviceAddress}.csv")
    }

    private fun getNewUnlabelledTempFile(fileDir: File, deviceAddress: String): File {
        return fileDir.resolve("${System.currentTimeMillis()}_${deviceAddress}.csv")
    }

    private fun startLogging() {
        endButton.isEnabled = true

        timer.base = SystemClock.elapsedRealtime()
        timer.format = "%s" // set the format for a chronometer
        timer.start()
        val isLabelSelected = spinner.selectedItemPosition != 0
        val fileDir = if (isLabelSelected) getRecordingFileDir(
            LocalDateTime.now(),
            spinner.selectedItem.toString()
        ) else GlobalValues.getSensorDataTempUnlabelledDir(context)
        fileDir.mkdirs()
        recordingName = fileDir.toString()

        //spinner.setSelection(0)
        for (device in devices.getConnected()) {
            device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
            device.startMeasuring()
            val file = if (isLabelSelected) getRecordingFile(
                fileDir,
                device.address
            ) else getNewUnlabelledTempFile(fileDir, device.address)
            if(!isLabelSelected){
                unlabelledRecordingMap[device.address] = Pair(LocalDateTime.now(), file)
            }
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

    private fun stopLogging() {
        val isLabelSelected = spinner.selectedItemPosition != 0 && spinner.selectedItemPosition!= spinner.count-1
        if (!isLabelSelected) {
            val dialog =PostLabellingDialog(context, labelsStorage.getLabelsArray())
            dialog.setOnLabelSelectedListener{ label ->moveUnlabelledTempFiles(label) }
        }

        timer.stop()
        for (logger in xsLoggers) {
            logger.stop()
        }
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }
        if(isLabelSelected){
            moveUnlabelledTempFiles(spinner.selectedItem.toString())
        }
        spinner.setSelection(0)
        endButton.isEnabled = false
        xsLoggers.clear()

        addRecordingToUI(recordingName,timer.text.toString())
    }
    private fun moveUnlabelledTempFiles(label: String){
        val keys = unlabelledRecordingMap.keys.asIterable()
        for (deviceAddress in keys) {
                // Label has been selected during recording -> file must be moved
                val pair = unlabelledRecordingMap[deviceAddress]
                val time = pair!!.first
                val tempFile = pair.second
                val file = getRecordingFile(time, label, deviceAddress )
                file.parentFile!!.mkdirs()
                Files.copy(tempFile.toPath(), FileOutputStream(file))
                tempFile.delete()
        }
        //TODO: change from label to recording name as in other method execution
        addRecordingToUI(label,timer.text.toString())
        unlabelledRecordingMap.clear()
    }
    private fun addRecordingToUI(name:String, duration:String){
        recordingsManager.saveDuration(name, duration)
        recordingsAdapter.update()
        updateActivityCounts()
    }
    private fun updateActivityCounts() {
        activityCountTextView.text = recordingsManager.getNumberOfRecordings().toString()
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        numConnectedDevices = devices.getConnected().size

        val deviceLogger = xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }
        if (!connected && deviceLogger != null) {
            devices.get(deviceAddress)?.let {
                if (it.connectionState == XsensDotDevice.CONN_STATE_DISCONNECTED) {
                    uiHelper.buildAndShowAlert(
                        "The Device ${it.name} was disconnected!"
                    )
                    deviceLogger.stop()
                    it.stopMeasuring()
                    timer.stop()
                }
            }
        }
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }?.update(xsensDotData)
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {}
}
