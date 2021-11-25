package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotDevice
import com.xsens.dot.android.sdk.models.XsensDotPayload
import com.xsens.dot.android.sdk.recording.XsensDotRecordingManager
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
import kotlin.properties.Delegates
import android.widget.CompoundButton
import org.xmlpull.v1.XmlSerializer


class Page2Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface, RecordingInterface {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton
    private lateinit var xsLoggers: ArrayList<XsensDotLogger>
    private lateinit var uiHelper: UIHelper
    private lateinit var spinner: Spinner
    private lateinit var xsRecorders: ArrayList<Pair<XsensDotRecordingManager, String>>
    private lateinit var recordingOnDevicesSwitch: SwitchCompat
    private var recordingOnDevices by Delegates.notNull<Boolean>()

    private var numConnectedDevices = 0
    private var numDevices = 5

    override fun activityCreated(activity: Activity) {
        this.context = activity
        this.uiHelper = UIHelper(this.context)

        timer = activity.findViewById(R.id.timer)
        startButton = activity.findViewById(R.id.buttonStart)
        endButton = activity.findViewById(R.id.buttonEnd)
        spinner = activity.findViewById(R.id.spinner)
        recordingOnDevicesSwitch = activity.findViewById(R.id.switch1)

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

        recordingOnDevicesSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            // do something, the isChecked will be
            // true if the switch is in the On position
            recordingOnDevices = b
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
            if (!recordingOnDevices) {
                if (numConnectedDevices >= numDevices) {
                    startLogging()
                } else {
                    Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
                }
            } else {
                startRecording()
            }
        }

        endButton.setOnClickListener {
            stopLogging()
        }
    }

    override fun requestFlashInfo() {
        for (recorder in xsRecorders) {
            recorder.first.requestFlashInfo()
        }
    }

    override fun canStartRecording(address: String?, b: Boolean) {
        xsRecorders.find {
                pair: Pair<XsensDotRecordingManager, String> -> pair.second == address }?.first?.setActive(b)
    }

    private fun startRecording() {
        assert(recordingOnDevices)
        for (device in devices) {
            val mManager = XsensDotRecordingManager(context, device, RecordingHandler(this))
            xsRecorders.add(Pair(mManager, device.address))
            mManager.enableDataRecordingNotification()
        }
        //only start recording if all managers are set to active
        for (pair in xsRecorders) {
            if (pair.first.isActive) {
                pair.first.startRecording()
            }
        }
    }

    private fun startLogging() {
        // disable startButton
        endButton.isEnabled = true

        timer.base = SystemClock.elapsedRealtime()
        timer.format = "Time Running - %s" // set the format for a chronometer
        timer.start()

        val filename = File(this.context.getExternalFilesDir(null).toString() +
                "/${spinner.selectedItem}/" +
                "${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())}/dev/")
        filename.mkdirs()
        spinner.setSelection(0)
        for (device in devices.getConnected()) {
            device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
            device.startMeasuring()

            xsLoggers.add(
                XsensDotLogger(
                    this.context,
                    XsensDotLogger.TYPE_CSV,
                    XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION,
                    filename.absolutePath + "${device.address}.csv",
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

        for (logger in xsLoggers) {
            logger.stop()
        }
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }
        endButton.isEnabled = false
        xsLoggers.clear()
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
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
