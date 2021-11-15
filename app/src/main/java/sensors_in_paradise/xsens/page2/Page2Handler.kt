package sensors_in_paradise.xsens.page2

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Chronometer
import android.widget.Spinner
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotDeviceCallback
import com.xsens.dot.android.sdk.models.FilterProfileInfo
import com.xsens.dot.android.sdk.models.XsensDotDevice
import com.xsens.dot.android.sdk.utils.XsensDotLogger
import sensors_in_paradise.xsens.PageInterface
import sensors_in_paradise.xsens.R
import sensors_in_paradise.xsens.page1.Page1Handler
import java.util.logging.Logger

class Page2Handler(val devices: ArrayList<XsensDotDevice>) : PageInterface, XsensDotDeviceCallback {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton
    private lateinit var xsLogger: XsensDotLogger

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

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                startButton.isEnabled = false
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                startButton.isEnabled = position != 0
            }
        }

        startButton.setOnClickListener{
            timer.base = SystemClock.elapsedRealtime()
            timer.format = "Time Running - %s" // set the format for a chronometer
            timer.start()
            for (device in devices) {
                device.setMeasurementMode(1)
                device.startMeasuring()

            }
            //startMeasuring()
            //startLogging
        }

        endButton = activity.findViewById(R.id.buttonEnd)


        endButton.setOnClickListener{
            spinner.setSelection(0)
            timer.stop()
            startButton.isEnabled = false
            for (device in devices) {
                device.stopMeasuring()
            }
        }
    }

    override fun onXsensDotDataChanged(p0: String?, p1: XsensDotData?) {
        TODO("Not yet implemented")
    }

    override fun activityResumed() {
    }

    override fun onXsensDotConnectionChanged(p0: String?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotServicesDiscovered(p0: String?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotFirmwareVersionRead(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotTagChanged(p0: String?, p1: String?) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotBatteryChanged(p0: String?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }


    override fun onXsensDotInitDone(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotButtonClicked(p0: String?, p1: Long) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotPowerSavingTriggered(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onReadRemoteRssi(p0: String?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotOutputRateUpdate(p0: String?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotFilterProfileUpdate(p0: String?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onXsensDotGetFilterProfileInfo(p0: String?, p1: java.util.ArrayList<FilterProfileInfo>?) {
        TODO("Not yet implemented")
    }

    override fun onSyncStatusUpdate(p0: String?, p1: Boolean) {
        TODO("Not yet implemented")
    }
}