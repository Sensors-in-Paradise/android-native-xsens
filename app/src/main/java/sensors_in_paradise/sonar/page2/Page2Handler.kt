package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotDevice
import com.xsens.dot.android.sdk.models.XsensDotPayload
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import sensors_in_paradise.sonar.UIHelper
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

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
                context,
                R.array.activities_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        endButton.isEnabled = false

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                startButton.isEnabled = false
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                startButton.isEnabled = position != 0
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

    private fun startLogging() {
        endButton.isEnabled = true

        timer.base = SystemClock.elapsedRealtime()
        timer.format = "%s" // set the format for a chronometer
        timer.start()

        val filename = File(fileDirectory +
                "/${spinner.selectedItem}/" +
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()))
        filename.mkdirs()

        recordingName = filename.toString()

        spinner.setSelection(0)
        for (device in devices.getConnected()) {
            device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
            device.startMeasuring()

            xsLoggers.add(
                XsensDotLogger(
                    this.context,
                    XsensDotLogger.TYPE_CSV,
                    XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION,
                    filename.absolutePath + "/${device.address}.csv",
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
        spinner.setSelection(0)
        timer.stop()

        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }

        // Quick Fix: Waiting so logger can finish writing files
        Log.d("Logging", "Start")
        val waitTime: Long = 3000
        Handler(Looper.getMainLooper()).postDelayed({
            for (logger in xsLoggers) {
                logger.stop()
            }
            Log.d("Logging", "Stop")
        }, waitTime)

        endButton.isEnabled = false

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
