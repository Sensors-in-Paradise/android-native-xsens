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
import sensors_in_paradise.xsens.PageInterface
import sensors_in_paradise.xsens.R

class Page2Handler : PageInterface {
    private lateinit var context: Context
    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton

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

        timer = activity.findViewById<Chronometer>(R.id.timer)

        startButton = activity.findViewById<MaterialButton>(R.id.buttonStart)

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
        }
        endButton = activity.findViewById<MaterialButton>(R.id.buttonEnd)

        endButton.setOnClickListener{
            spinner.setSelection(0)
            timer.stop()
            startButton.isEnabled = false
        }
    }

    override fun activityResumed() {
    }
}