package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotDevice
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.XSENSArrayList
import sensors_in_paradise.sonar.util.UIHelper
import java.io.File

class Page2Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton

    private lateinit var spinner: Spinner
    private lateinit var recyclerViewRecordings: RecyclerView
    private lateinit var activityCountTextView: TextView

    private lateinit var recordingsAdapter: RecordingsAdapter

    private var numConnectedDevices = 0
    private var numDevices = 5

    private lateinit var recordingsManager: RecordingDataManager
    private lateinit var labelsStorage: LabelsStorage
    private lateinit var labelsAdapter: RecordingLabelsAdapter
    private lateinit var loggingManager: LoggingManager

    override fun activityCreated(activity: Activity) {
        this.context = activity

        timer = activity.findViewById(R.id.timer)
        startButton = activity.findViewById(R.id.buttonStart)
        endButton = activity.findViewById(R.id.buttonEnd)
        spinner = activity.findViewById(R.id.spinner)
        activityCountTextView = activity.findViewById(R.id.tv_activity_counts)

        recordingsManager = RecordingDataManager(
            File(context.dataDir, "recordingDurations.json"), GlobalValues.getSensorRecordingsBaseDir(context)
        )
        recyclerViewRecordings = activity.findViewById(R.id.recyclerViewRecordings)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewRecordings.layoutManager = linearLayoutManager
        recordingsAdapter = RecordingsAdapter(recordingsManager)
        recyclerViewRecordings.adapter = recordingsAdapter

        labelsStorage = LabelsStorage(context)
        loggingManager = LoggingManager(context, devices, labelsStorage, startButton, endButton, timer, spinner)
        loggingManager.setOnRecordingDone { recordingName, duration ->
            addRecordingToUI(
                recordingName,
                duration
            )
        }
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
                // startButton.isEnabled = false
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val isAddNewLabel = position == spinner.count - 1
                // startButton.isEnabled = position != 0 && !isAddNewLabel
                if (isAddNewLabel) {
                    handleCreateLabelRequested()
                }
            }
        }

        startButton.isEnabled = true
        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {
                loggingManager.startLogging()
            } else {
                Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            }
        }

        endButton.setOnClickListener {
            loggingManager.stopLogging()
        }

        updateActivityCounts()
    }

    private fun handleCreateLabelRequested() {
        val currentLabels = labelsStorage.getLabelsArray()
        val promptInterface = { value: String ->
            labelsStorage.addLabel(value.toLowerCase())
            labelsAdapter.insert(value.toLowerCase(), 1)
            spinner.setSelection(1)
        }

        val acceptanceInterface = { text: String ->
            val alreadyAdded = currentLabels.contains(text)
            val valid = text != ""
            if (valid) {
                Pair(
                    !alreadyAdded,
                    if (alreadyAdded) "Label already added" else null
                )
            } else {
                Pair(
                    false,
                    "Invalid label"
                )
            }
        }
        TextInputDialog(
            context, "Add new label",
            promptInterface, "Label", acceptanceInterface = acceptanceInterface
        ).setCancelListener { _ -> spinner.setSelection(0) }
    }

    private fun addRecordingToUI(name: String, duration: String) {
        recordingsManager.addRecordingAt0(name, duration)
        recordingsAdapter.notifyItemInserted(0)
        updateActivityCounts()
    }

    private fun updateActivityCounts() {
        activityCountTextView.text = recordingsManager.getNumberOfRecordings().toString()
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        numConnectedDevices = devices.getConnected().size

        val deviceLogger =
            loggingManager.xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }
        if (!connected && deviceLogger != null) {
            devices.get(deviceAddress)?.let {
                if (it.connectionState == XsensDotDevice.CONN_STATE_DISCONNECTED) {
                    UIHelper.showAlert(context,
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
        loggingManager.xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }
            ?.update(xsensDotData)
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {}
}
