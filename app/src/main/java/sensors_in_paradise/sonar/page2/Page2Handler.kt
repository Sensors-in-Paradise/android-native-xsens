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
import com.xsens.dot.android.sdk.models.XsensDotRecordingFileInfo
import com.xsens.dot.android.sdk.models.XsensDotRecordingState
import com.xsens.dot.android.sdk.recording.XsensDotRecordingManager
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.UIHelper
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class Page2Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface, RecordingInterface {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton
    private lateinit var exportButton: MaterialButton
    private lateinit var xsLoggers: ArrayList<XsensDotLogger>
    private lateinit var uiHelper: UIHelper
    private lateinit var spinner: Spinner
    private lateinit var xsRecorders: ArrayList<Pair<XsensDotRecordingManager, String>>
    private lateinit var recordingOnDevicesSwitch: SwitchCompat
    private lateinit var exportingList: XSensExportingList
    private lateinit var recordingActivityList: ArrayList<String>
    private var recordingOnDevices by Delegates.notNull<Boolean>()
    private var readyToExport by Delegates.notNull<Boolean>()

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
        exportButton = activity.findViewById(R.id.buttonExport)

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
            exportButton.visibility = if (b) View.VISIBLE else View.INVISIBLE
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
        xsRecorders = ArrayList()
        exportingList = XSensExportingList()
        readyToExport = false
        startButton.setOnClickListener {
            recordingOnDevicesSwitch.isClickable = false
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
            when (recordingOnDevices) {
                true -> { stopRecording() }
                false -> { stopLogging() }
            }

            recordingOnDevicesSwitch.isClickable = true
        }
        exportButton.setOnClickListener {
            startExporting()
            exportButton.isClickable = false
        }
    }

    override fun requestFlashInfo() {
        for (recorder in xsRecorders) {
            recorder.first.requestFlashInfo()
        }
    }

    // sets a recorder to active, since the available disk space is large enough(>50%)
    override fun canStartRecording(address: String?, b: Boolean) {
        xsRecorders.find { pair: Pair<XsensDotRecordingManager, String> -> pair.second == address }?.first?.isActive =
            b
    }

    override fun recordingStarted(
        address: String?,
        recordingId: Int,
        success: Boolean,
        xSensDotRecordingState: XsensDotRecordingState?
    ) {
        xsRecorders.find { pair: Pair<XsensDotRecordingManager, String> -> pair.second == address }
            .let {
            if (it != null && xSensDotRecordingState != null) {
                when (xSensDotRecordingState) {
                    XsensDotRecordingState.success -> {
                        Toast.makeText(
                            this.context,
                            "Recording started on device: $address!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    XsensDotRecordingState.fail -> {
                        uiHelper.buildAndShowAlert(
                            "Recording failed to start on device: $address!"
                        )
                    }
                    else -> {
                        Toast.makeText(
                            this.context,
                            "RecordingState unexpected!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    override fun recordingStopped(
        address: String?,
        recordingId: Int,
        success: Boolean,
        xSensDotRecordingState: XsensDotRecordingState?
    ) {
        xsRecorders.find { pair: Pair<XsensDotRecordingManager, String> -> pair.second == address }
        .let {
            if (it != null && xSensDotRecordingState != null) {
                when (xSensDotRecordingState) {
                    XsensDotRecordingState.success -> {
                        Toast.makeText(
                            this.context,
                            "Recording stopped on device: $address!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    XsensDotRecordingState.fail -> {
                        uiHelper.buildAndShowAlert(
                            "Recording failed to start on device: $address!"
                        )
                    }
                    else -> {
                        Toast.makeText(
                            this.context,
                            "RecordingState unexpected!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                // it.first.requestRecordingTime() can be called here if we want to ue the recording time somehow
            }
        }
    }
    private fun startRecording() {
        assert(recordingOnDevices)
        for (device in devices) {
            val mManager = XsensDotRecordingManager(context, device, RecordingHandler(this))
            xsRecorders.add(Pair(mManager, device.address))
            mManager.enableDataRecordingNotification()
        }
        // only start recording if all managers are set to active
        for (pair in xsRecorders) {
            if (!pair.first.isActive) {
                Toast.makeText(
                    this.context,
                    "Can't start recording yet, since some devices don't have enough storage left",
                    Toast.LENGTH_SHORT).show()
                return
            }
        }
        // start recording
        for (pair in xsRecorders) {

            pair.first.startRecording()
        }
        recordingActivityList.add(spinner.selectedItem.toString())
    }

    private fun stopRecording() {
        for (pair in xsRecorders) {
            pair.first.stopRecording()
        }
    }

    // probably want to do this on all devices
    private fun startExporting() {
        xsRecorders.forEach {
            it.first.requestFileInfo()
        }
        if (exportingList.size == numConnectedDevices) {
            xsRecorders.forEach { pair ->
                val mSelectExportedDataIds = ByteArray(3)
                with(pair) {
                    mSelectExportedDataIds[0] = XsensDotRecordingManager.RECORDING_DATA_ID_TIMESTAMP
                    mSelectExportedDataIds[1] = XsensDotRecordingManager.RECORDING_DATA_ID_ORIENTATION
                    mSelectExportedDataIds[2] = XsensDotRecordingManager.RECORDING_DATA_ID_CALIBRATED_ACC
                    first.selectExportedData(mSelectExportedDataIds)
                    first.startExporting(
                        ArrayList(exportingList
                            .filter { triple -> triple.first == second && triple.second != null }
                            .map { t -> t.second }))
                }
            }
        }
    }

    override fun canExport(address: String?, list: ArrayList<XsensDotRecordingFileInfo>?) {
        Toast.makeText(
            this.context,
            "Can start exporting on device: $address" +
                    " with fileId: ${list?.get(0)}," +
                    " fileName: ${list?.get(1)}" +
                    " and dataSize: ${list?.get(2)}",
            Toast.LENGTH_SHORT).show()
        list?.forEachIndexed { index, recordingFile ->
            exportingList.add(Triple(address, recordingFile, recordingActivityList[index])) }
    }

    override fun cantExport(address: String?, list: ArrayList<XsensDotRecordingFileInfo>?) {
        uiHelper.buildAndShowAlert("Can't export to device: $address")
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
