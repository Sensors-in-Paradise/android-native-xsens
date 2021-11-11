package sensors_in_paradise.xsens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_capture.*

class CaptureActivity : AppCompatActivity() {

    private lateinit var timer: Chronometer
    private lateinit var startButton: MaterialButton
    private lateinit var endButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        val spinner: Spinner = findViewById(R.id.spinner)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        timer = findViewById<Chronometer>(R.id.timer)

        startButton = findViewById<MaterialButton>(R.id.buttonStart)

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
        endButton = findViewById<MaterialButton>(R.id.buttonEnd)

        endButton.setOnClickListener{
            spinner.setSelection(0)
            timer.stop()
            startButton.isEnabled = false
        }
    }


}
