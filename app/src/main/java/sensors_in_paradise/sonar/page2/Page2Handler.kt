package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotDevice
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.XSENSArrayList
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.util.UIHelper
import java.io.File

class Page2Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface,
    RecordingInterface {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton
    private lateinit var exportButton: MaterialButton
    private var recordingOnDevice: Boolean = false

    private lateinit var recyclerViewRecordings: RecyclerView
    private lateinit var activityCountTextView: TextView

    private lateinit var recordingsAdapter: RecordingsAdapter
    private lateinit var recordingHandler: RecordingHandler

    private var numConnectedDevices = 0
    private var numDevices = 5

    private lateinit var recordingsManager: RecordingDataManager

    private lateinit var labelTV: TextView
    private lateinit var personTV: TextView
    private lateinit var loggingManager: LoggingManager

    override fun activityCreated(activity: Activity) {
        this.context = activity

        timer = activity.findViewById(R.id.timer)
        startButton = activity.findViewById(R.id.buttonStart)
        endButton = activity.findViewById(R.id.buttonEnd)
        exportButton = activity.findViewById(R.id.buttonExport)
        activityCountTextView = activity.findViewById(R.id.tv_activity_counts)

        recordingsManager = RecordingDataManager(
            File(context.dataDir, "recordingDurations.json"),
            GlobalValues.getSensorRecordingsBaseDir(context)
        )
        recyclerViewRecordings = activity.findViewById(R.id.recyclerViewRecordings)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewRecordings.layoutManager = linearLayoutManager
        recordingsAdapter = RecordingsAdapter(recordingsManager)
        recyclerViewRecordings.adapter = recordingsAdapter

        labelTV = activity.findViewById(R.id.tv_activity_captureFragment)
        personTV = activity.findViewById(R.id.tv_person_captureFragment)
        labelTV.setOnClickListener {
            PersistentStringArrayDialog(
                context,
                "Select an activity Label",
                GlobalValues.getActivityLabelsJSONFile(context)
            ) { label ->
                labelTV.setText(label)
            }
        }
        personTV.setOnClickListener {
            PersistentStringArrayDialog(
                context,
                "Select a Person",
                GlobalValues.getPeopleJSONFile(context)
            ) { person ->
                personTV.setText(person)
            }
        }
        endButton.isEnabled = false

        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {
                if (recordingOnDevice) startRecording() else loggingManager.startLogging()
            } else {
                Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            }
        }
        startButton.isEnabled = true
        endButton.setOnClickListener {
            if (recordingOnDevice) stopRecording() else loggingManager.stopLogging()
        }

        exportButton.setOnClickListener { startExporting() }

        loggingManager =
            LoggingManager(context, devices, startButton, endButton, timer, labelTV, personTV)
        loggingManager.setOnRecordingDone { recordingName, duration ->
            addRecordingToUI(
                recordingName,
                duration
            )
        }
        updateActivityCounts()
    }

    private fun addRecordingToUI(name: String, duration: String) {
        recordingsManager.addRecordingAt0(name, duration)
        recordingsAdapter.notifyItemInserted(0)
        updateActivityCounts()
    }

    private fun updateActivityCounts() {
        val numberOfRecodings = recordingsManager.getNumberOfRecordings()
        var text = " "
        for ((activity, number) in numberOfRecodings) {
            text += "$activity: $number | "
        }
        activityCountTextView.text = text.trimEnd('|', ' ')
    }

    override fun activityResumed() {
        val sharedPreferences = context.getSharedPreferences("Recording", MODE_PRIVATE)
        recordingOnDevice = sharedPreferences.getBoolean("onDevice", false)
        if (recordingOnDevice) exportButton.visibility = VISIBLE else exportButton.visibility = GONE
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        numConnectedDevices = devices.getConnected().size

        val deviceLogger =
            loggingManager.xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }
        if (!connected && deviceLogger != null) {
            devices.get(deviceAddress)?.let {
                if (it.connectionState == XsensDotDevice.CONN_STATE_DISCONNECTED) {
                    UIHelper.showAlert(
                        context,
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

    override fun startRecording() {
        recordingHandler = RecordingHandler(context, devices, this)
        recordingHandler.startRecording()
    }

    override fun stopRecording() {
        recordingHandler.stopRecording()
    }

    override fun startExporting() {
        recordingHandler.startExporting()
        disableAllButtons()
    }

    private fun disableAllButtons() {
        startButton.isEnabled = false
        endButton.isEnabled = false
        exportButton.isEnabled = false
    }

    override fun exportingFinished() {
        enableButtons()
    }

    private fun enableButtons() {

    }

}
