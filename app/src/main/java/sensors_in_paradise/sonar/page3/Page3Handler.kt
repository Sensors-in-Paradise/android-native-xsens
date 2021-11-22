package sensors_in_paradise.sonar.page3

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.events.XsensDotData
// import org.tensorflow.lite.DataType
// import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import kotlin.collections.ArrayList

// import sensors_in_paradise.sonar.ml.TestModel
// import java.io.File
// import java.nio.ByteBuffer

class Page3Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private val predictions = ArrayList<Prediction>()
    private var sensorData = mutableMapOf<String, MutableList<XsensDotData>>()
    private lateinit var predictButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var timer: Chronometer

    private var isRunning = false
    private val numDevices = 0
    private var numConnectedDevices = 0


    private fun _toggleButtons() {
        startButton.isEnabled = !(startButton.isEnabled)
        stopButton.isEnabled = !(stopButton.isEnabled)
    }

    private fun _formatData() {

    }

    private fun _stopDataCollection() {
        timer.stop()
        for (device in devices.getConnected()) {
            //device.stopMeasuring()
        }

        _formatData()

        _toggleButtons()
        isRunning = false
    }

    override fun activityCreated(activity: Activity) {

        this.activity = activity
        this.context = activity

        // Inititalising prediction RV
        recyclerView = activity.findViewById(R.id.rv_prediction)
        val prediction1 = Prediction("Squats", "90%")
        val prediction2 = Prediction("Running", "75%")


        predictions.add(prediction1)
        predictions.add(prediction2)
        adapter = PredictionsAdapter(predictions)
        recyclerView.adapter = adapter
        Log.d("ADAPTER", "222SIZE PREDICTION: " + adapter.itemCount)
        Toast.makeText(context, "Moin", Toast.LENGTH_LONG).show()
        adapter.notifyDataSetChanged()

        // Initialising data array



        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        startButton = activity.findViewById(R.id.button_start_predict)
        stopButton = activity.findViewById(R.id.button_stop_predict)
        stopButton.isEnabled = false

        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {

                for(device in devices) {
                    sensorData.put(device.address, mutableListOf<XsensDotData>())
                }

                timer.base = SystemClock.elapsedRealtime()
                timer.start()

                _toggleButtons()
                isRunning = true
            }
        }

        stopButton.setOnClickListener {

            _stopDataCollection()

            println(sensorData)

        }

        predictButton = activity.findViewById(R.id.button_predict_predict)
        predictButton.setOnClickListener {
        /*
        // get data and model
        var data = Page3Handler::class.java.getResource("/standing_array.raw").readBytes()
        val model = TestModel.newInstance(context)
        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 9), DataType.FLOAT32)
        inputFeature0.loadBuffer(ByteBuffer.wrap(data))
        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        // Releases model resources if no longer used.
        model.close()
        //this is our Output
        val test = outputFeature0.floatArray
        */
        }
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        //TODO("Not yet implemented
        if (connected) {
            numConnectedDevices += 1
        } else {
            numConnectedDevices -= 1
        }
        println("#########################################################################################")
        println(numConnectedDevices)

        if (isRunning && numConnectedDevices < numDevices) {
            _stopDataCollection()
            Toast.makeText(context, "Connection to device(s) lost!", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        //if (!isRunning) return

        sensorData[deviceAddress]?.add(xsensDotData)

    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        //TODO("Not yet implemented")
    }
}
