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
    private lateinit var spinnerActivity: Spinner
    private lateinit var spinnerPerson: Spinner
    private lateinit var recyclerViewRecordings: RecyclerView
    private lateinit var activityCountTextView: TextView

    private var fileDirectory: String = ""
    private lateinit var recordingsAdapter: RecordingsAdapter

    private var numConnectedDevices = 0
    private var numDevices = 5

    private var personSelected = false
    private var labelSelected = false

    private lateinit var recordingName: String
    private lateinit var recordingsManager: RecordingDataManager
    private lateinit var peopleSelectionStorage: RecordingLabelsStorage
    private lateinit var peopleSelectionAdapter: RecordingLabelsAdapter
    private lateinit var labelsStorage: RecordingLabelsStorage
    private lateinit var labelsAdapter: RecordingLabelsAdapter
    override fun activityCreated(activity: Activity) {
        this.context = activity
        this.uiHelper = UIHelper(this.context)
        fileDirectory = this.context.getExternalFilesDir(null).toString()

        timer = activity.findViewById(R.id.timer)
        startButton = activity.findViewById(R.id.buttonStart)
        endButton = activity.findViewById(R.id.buttonEnd)
        spinnerActivity = activity.findViewById(R.id.spinner_activity)
        spinnerPerson = activity.findViewById(R.id.spinner_person)
        activityCountTextView = activity.findViewById(R.id.tv_activity_counts)

        // List of previous recordings
        recordingsManager = RecordingDataManager(fileDirectory, RecordingPreferences(context))
        recyclerViewRecordings = activity.findViewById(R.id.recyclerViewRecordings)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewRecordings.layoutManager = linearLayoutManager
        recordingsAdapter = RecordingsAdapter(recordingsManager)
        recyclerViewRecordings.adapter = recordingsAdapter

        // Person selection
        peopleSelectionStorage = RecordingLabelsStorage(context, "people.json")
        peopleSelectionAdapter = RecordingLabelsAdapter(activity)

        peopleSelectionAdapter.setDeleteButtonClickListener(object : RecordingLabelsAdapter.ClickInterface {
            override fun onDeleteButtonPressed(person: String) {
                ApproveDialog(context, "Do you really want to delete the person $person?"
                ) { p0, p1 ->
                    peopleSelectionStorage.removeLabel(person)
                    spinnerPerson.setSelection(0)
                    peopleSelectionAdapter.remove(person)
                }
            }
        })

        spinnerPerson.adapter = peopleSelectionAdapter
        peopleSelectionAdapter.add("Select person...")
        for (s in peopleSelectionStorage.getLabelsArray()) {
            peopleSelectionAdapter.add(s)
        }
        peopleSelectionAdapter.add("Add new person...")

        spinnerPerson.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                startButton.isEnabled = false
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                personSelected = position != 0
                val isAddNewLabel = position == spinnerPerson.count - 1
                startButton.isEnabled = position != 0 && !isAddNewLabel && labelSelected

                if (isAddNewLabel) {
                    handleCreateLabelRequested(spinnerPerson, peopleSelectionAdapter, peopleSelectionStorage)
                }
            }
        }






        // Activity label selection
        labelsStorage = RecordingLabelsStorage(context, "recordingLabels.json")

        labelsAdapter = RecordingLabelsAdapter(activity)
        labelsAdapter.setDeleteButtonClickListener(object : RecordingLabelsAdapter.ClickInterface {
            override fun onDeleteButtonPressed(label: String) {
               ApproveDialog(context, "Do you really want to delete the label $label?"
               ) { p0, p1 ->
                   labelsStorage.removeLabel(label)
                   spinnerActivity.setSelection(0)
                   labelsAdapter.remove(label)
               }
            }
        })

        spinnerActivity.adapter = labelsAdapter
        labelsAdapter.add("Select label...")
        for (s in labelsStorage.getLabelsArray()) {
            labelsAdapter.add(s)
        }
        labelsAdapter.add("Add new label...")
        endButton.isEnabled = false

        spinnerActivity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                startButton.isEnabled = false
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                labelSelected = position != 0
                val isAddNewLabel = position == spinnerActivity.count - 1
                startButton.isEnabled = position != 0 && !isAddNewLabel && personSelected

                if (isAddNewLabel) {
                    handleCreateLabelRequested(spinnerActivity, labelsAdapter, labelsStorage)
                }
            }
        }

        xsLoggers = ArrayList()
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

    private fun handleCreateLabelRequested(spinner: Spinner, spinnerAdapter: RecordingLabelsAdapter, spinnerStorage: RecordingLabelsStorage) {
        val currentLabels = spinnerStorage.getLabelsArray()
        val promptInterface = object : TextInputDialog.PromptInterface {
            override fun onInputSubmitted(input: String) {
                spinnerStorage.addLabel(input.toLowerCase())
                spinnerAdapter.insert(input.toLowerCase(), 1)
                spinner.setSelection(1)
            }
        }

        val acceptanceInterface = object : AcceptanceInterface {
            override fun onInputChanged(text: String): Pair<Boolean, String?> {
                val alreadyAdded = currentLabels.contains(text)
                val valid = text != ""
                if (valid) {
                    return Pair(
                        !alreadyAdded,
                        if (alreadyAdded) "Label already added" else null
                    )
                }
                return Pair(
                    false,
                    "Invalid label"
                )
            }
        }
        TextInputDialog(context, "Add new label",
            promptInterface, "Label", acceptanceInterface = acceptanceInterface
        ).setCancelListener { _, -> spinner.setSelection(0) }
    }

    private fun startLogging() {
        endButton.isEnabled = true

        timer.base = SystemClock.elapsedRealtime()
        timer.format = "%s" // set the format for a chronometer
        timer.start()

        val time = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
        val fileDir = GlobalValues.getSensorDataBaseDir(context).resolve(
                spinnerPerson.selectedItem.toString()).resolve(
                spinnerActivity.selectedItem.toString()).resolve(time)
        fileDir.mkdirs()

        recordingName = fileDir.toString()

        spinnerActivity.setSelection(0)
        for (device in devices.getConnected()) {
            device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
            device.startMeasuring()
            val file = fileDir.resolve("${device.address}.csv")
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
                    0))
        }
    }

    private fun stopLogging() {
        spinnerActivity.setSelection(0)
        timer.stop()
        for (logger in xsLoggers) {
            logger.stop()
        }
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }
        endButton.isEnabled = false
        xsLoggers.clear()

        recordingsManager.saveDuration(recordingName, timer.text.toString())
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
