package sensors_in_paradise.sonar.page3

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotPayload
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.util.PredictionHelper
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.ml.XsensTest02
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import kotlin.collections.ArrayList
import java.nio.ByteBuffer
import kotlin.math.round

class Page3Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private lateinit var predictButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var timer: Chronometer

    private lateinit var predictionHelper: PredictionHelper
    private val predictions = ArrayList<Prediction>()
    private val rawSensorDataMap = mutableMapOf<String, MutableList<Pair<Long, FloatArray>>>()
    private var sensorDataByteBuffer: ByteBuffer? = null

    private val numDevices = 5
    private var numConnectedDevices = 0
    private var isRunning = false

    private fun toggleButtons() {
        startButton.isEnabled = !(startButton.isEnabled)
        stopButton.isEnabled = !(stopButton.isEnabled)
    }

    private fun clearBuffers() {
        for ((_, deviceDataList) in rawSensorDataMap) {
            deviceDataList.clear()
        }
        sensorDataByteBuffer = null
    }

    private fun stopDataCollection() {
        timer.stop()
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }

        sensorDataByteBuffer = predictionHelper.processSensorData()

        toggleButtons()
        isRunning = false
    }

    private fun addPredictionViews(output: FloatArray) {
        predictions.clear()

        val outputLabelMap = mapOf(
            0 to "Walking",
            1 to "Squats",
            2 to "Running",
            3 to "Stairs Down",
            4 to "Stairs up",
            5 to "Standing"
        ).withDefault { "" }

        for (i in 0..output.size - 1) {
            val procentage = round(output[i] * 10000) / 100
            val prediction = Prediction(outputLabelMap[i]!!, procentage.toString() + "%")
            predictions.add(prediction)
        }
        adapter.notifyDataSetChanged()
    }

    override fun activityCreated(activity: Activity) {

        this.activity = activity
        this.context = activity

        predictionHelper = PredictionHelper(context, rawSensorDataMap)

        // Inititalising prediction RV
        recyclerView = activity.findViewById(R.id.rv_prediction)
        adapter = PredictionsAdapter(predictions)
        recyclerView.adapter = adapter

        // Initialising data array
        for ((_, address) in GlobalValues.sensorTagMap) {
            rawSensorDataMap.put(address, mutableListOf<Pair<Long, FloatArray>>())
        }

        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        startButton = activity.findViewById(R.id.button_start_predict)
        stopButton = activity.findViewById(R.id.button_stop_predict)
        stopButton.isEnabled = false

        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {

                clearBuffers()

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
                val model = XsensTest02.newInstance(context)

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(
                    intArrayOf(1, predictionHelper.dataVectorSize, predictionHelper.dataLineFloatSize),
                    DataType.FLOAT32)
                inputFeature0.loadBuffer(sensorDataByteBuffer!!)

                // Runs model inference and gets result
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                model.close()

                addPredictionViews(outputFeature0.floatArray)
            } else {
                Toast.makeText(context, "Please measure an activity first!",
                    Toast.LENGTH_SHORT).show()
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

        val timeStamp: Long = xsensDotData.getSampleTimeFine()
        val quat: FloatArray = xsensDotData.getQuat()
        val freeAcc: FloatArray = xsensDotData.getFreeAcc()

        rawSensorDataMap[deviceAddress]?.add(Pair(timeStamp, quat + freeAcc))
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // TODO("Not yet implemented")
    }
}
