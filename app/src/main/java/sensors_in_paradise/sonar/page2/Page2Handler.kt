package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.view.View
import android.os.SystemClock
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
import sensors_in_paradise.sonar.page1.XSENSArrayList
import java.io.File
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

    private lateinit var recyclerViewRecordings: RecyclerView
    private lateinit var activityCountTextView: TextView

    private lateinit var recordingsAdapter: RecordingsAdapter

    private var numConnectedDevices = 0
    private var numDevices = 5

    private lateinit var recordingsManager: RecordingDataManager

    private lateinit var labelTV: TextView
    private lateinit var personTV: TextView
    private lateinit var loggingManager: LoggingManager

    override fun activityCreated(activity: Activity) {
        this.context = activity
        this.uiHelper = UIHelper(this.context)

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

        labelTV = activity.findViewById(R.id.tv_activity_captureFragment)
        personTV = activity.findViewById(R.id.tv_person_captureFragment)
        labelTV.setOnClickListener {
            PersistentStringArrayDialog(
                context,
                "Activity Labels",
                File(context.dataDir, "labels.json")
            ) { label ->
                labelTV.setText(label)
            }
        }
        personTV.setOnClickListener {
            PersistentStringArrayDialog(
                context,
                "Persons",
                File(context.dataDir, "people2.json")
            ) { person ->
                personTV.setText(person)
            }
        }
        endButton.isEnabled = false

        xsLoggers = ArrayList()
        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {
                loggingManager.startLogging()
            } else {
                Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            }
        }
        startButton.isEnabled = true
        endButton.setOnClickListener {
            loggingManager.stopLogging()
        }

        updateActivityCounts()
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
        /*val filename = File(fileDirectory +
                "/${spinner.selectedItem}/" +
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()))
        filename.mkdirs()*/
        val time = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
        val fileDir = GlobalValues.getSensorDataBaseDir(context).resolve(
                spinner.selectedItem.toString()).resolve(time)
        fileDir.mkdirs()
        // recordingName = filename.toString()
        recordingName = fileDir.toString()

        spinner.setSelection(0)
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
