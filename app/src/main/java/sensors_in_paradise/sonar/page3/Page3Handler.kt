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
import com.xsens.dot.android.sdk.models.XsensDotPayload
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
// import org.tensorflow.lite.DataType
// import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.ml.XsensTest
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import kotlin.collections.ArrayList

import java.nio.ByteBuffer

class Page3Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private lateinit var predictButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var timer: Chronometer

    private val predictions = ArrayList<Prediction>()
    private var rawSensorDataMap = mutableMapOf<String, MutableList<Pair<FloatArray, FloatArray>>>()
    private var sensorDataByteBuffer: ByteBuffer? = null

    private var isRunning = false
    private var numConnectedDevices = 0

    private val numDevices = 5
    private val sizeOfFloat = 4
    private val numQuats = 4
    private val numFreeAccs = 3

    val dataLineByteSize = sizeOfFloat * (numQuats + numFreeAccs) * numDevices
    var numDataLines = 0

    val sensorTagMap = mapOf(
        "LF" to "D4:22:CD:00:06:7B",
        "LW" to "D4:22:CD:00:06:89",
        "ST" to "D4:22:CD:00:06:7F",
        "RW" to "D4:22:CD:00:06:7D",
        "RF" to "D4:22:CD:00:06:72"
    )

    private fun toggleButtons() {
        startButton.isEnabled = !(startButton.isEnabled)
        stopButton.isEnabled = !(stopButton.isEnabled)
    }

    private fun createByteBuffer() {
        // TODO deal with empty lines of Data Collection
        val minDataLines = rawSensorDataMap.minOfOrNull { it.value.size }
        if (minDataLines == null) return

        numDataLines = minDataLines

        var floatArray = FloatArray(0)
        for (row in 0..numDataLines - 1) {
            var lineFloatArray = FloatArray(0)
            for ((_, deviceDataList) in rawSensorDataMap) {
                lineFloatArray = lineFloatArray + (deviceDataList[row].first + deviceDataList[row].second)
            }
            floatArray = floatArray + lineFloatArray
        }

        sensorDataByteBuffer = ByteBuffer.allocate(numDataLines * dataLineByteSize)
        sensorDataByteBuffer?.position(0)
        for (value in floatArray) {
            sensorDataByteBuffer?.putFloat(value)
        }
    }

    private fun stopDataCollection() {
        timer.stop()
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }

        createByteBuffer()

        toggleButtons()
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
        adapter.notifyDataSetChanged()

        // Initialising data array
        for ((_, address) in sensorTagMap) {
            rawSensorDataMap.put(address, mutableListOf<Pair<FloatArray, FloatArray>>())
        }

        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        startButton = activity.findViewById(R.id.button_start_predict)
        stopButton = activity.findViewById(R.id.button_stop_predict)
        stopButton.isEnabled = false

        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {

                for (device in devices.getConnected()) {
                    device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
                    device.startMeasuring()
                }

                timer.base = SystemClock.elapsedRealtime()
                timer.start()

                toggleButtons()
                isRunning = true
            } else {
                Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            }
        }

        stopButton.setOnClickListener {

            stopDataCollection()
        }

        predictButton = activity.findViewById(R.id.button_predict_predict)
        predictButton.setOnClickListener {
            if (sensorDataByteBuffer != null) {
                // get data and model
                val model = XsensTest.newInstance(context)
                // Creates inputs for reference.
                // ----> dimensions: amount, numDataLines * dataLineByteSize
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 180, 35), DataType.FLOAT32)
                // TODO: add the following line
                // inputFeature0.loadBuffer(sensorDataByteBuffer)
                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                // Releases model resources if no longer used.
                model.close()
                //this is our Output, first Value is for walking, second for squats
                val test = outputFeature0.floatArray
                // TODO: make result visible on the screen!
            } else {
                Toast.makeText(context, "Please measure an activity first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {

        numConnectedDevices = devices.getConnected().size

        if (isRunning && numConnectedDevices < numDevices) {
            stopDataCollection()
            Toast.makeText(context, "Connection to device(s) lost!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {

        val quat: FloatArray = xsensDotData.getQuat()
        val freeAcc: FloatArray = xsensDotData.getFreeAcc()

        rawSensorDataMap[deviceAddress]?.add(Pair(quat, freeAcc))
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // TODO("Not yet implemented")
    }
}
