package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Chronometer
import android.widget.Spinner
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotPayload
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Page2Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton
    private lateinit var xsLoggers: ArrayList<XsensDotLogger>

    override fun activityCreated(activity: Activity) {
        this.context = activity

        val spinner: Spinner = activity.findViewById(R.id.spinner)

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

        timer = activity.findViewById(R.id.timer)

        startButton = activity.findViewById(R.id.buttonStart)

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
            timer.base = SystemClock.elapsedRealtime()
            timer.format = "Time Running - %s" // set the format for a chronometer
            timer.start()
            val filename = File(this.context.getExternalFilesDir(null).toString() +
                    "/${spinner.selectedItem}/" +
                    "${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())}/dev/")
            filename.mkdirs()
            Log.d(devices.getConnected().toString(), "connected devices")
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

        endButton = activity.findViewById(R.id.buttonEnd)
        endButton.setOnClickListener {
            spinner.setSelection(0)
            timer.stop()
            startButton.isEnabled = false
            for (logger in xsLoggers) {
                logger.stop()
            }
            for (device in devices.getConnected()) {
                device.stopMeasuring()
            }
        }
    }

    override fun activityResumed() {}

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }?.update(xsensDotData)
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
    }
}