package sensors_in_paradise.sonar.page3

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.Toast
import sensors_in_paradise.sonar.util.UIHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotPayload
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.util.PredictionHelper
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.XSENSArrayList
import sensors_in_paradise.sonar.ml.Lstmmodel118
import kotlin.collections.ArrayList
import java.nio.ByteBuffer
import kotlin.math.round

class Page3Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private lateinit var predictionButton: MaterialButton
    private lateinit var timer: Chronometer

    private lateinit var predictionHelper: PredictionHelper
    private val predictions = ArrayList<Prediction>()
    private val rawSensorDataMap = mutableMapOf<String, MutableList<Pair<Long, FloatArray>>>()
    private var sensorDataByteBuffer: ByteBuffer? = null

    private var lastPrediction: Long = 0

    private val numDevices = 5
    private var numConnectedDevices = 0
    private var isRunning = false

    private lateinit var predictionModel: Lstmmodel118
    private lateinit var mainHandler: Handler

    private val updatePredictionTask = object : Runnable {
        override fun run() {
            processAndPredict()
            mainHandler.postDelayed(this, 4000)
        }
    }

    private fun togglePrediction() {
        if (isRunning) {
            predictionButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
            predictionButton.setText("Start")
            stopDataCollection()
        } else {
            predictionButton.setIconResource(R.drawable.ic_baseline_stop_24)
            predictionButton.setText("Stop")
            startDataCollection()
        }
    }

    private fun clearBuffers() {
        for ((_, deviceDataList) in rawSensorDataMap) {
            deviceDataList.clear()
        }
        sensorDataByteBuffer = null
    }

    private fun startDataCollection() {
        if (numConnectedDevices >= numDevices) {
            clearBuffers()
            lastPrediction = 0L

            for (device in devices.getConnected()) {
                device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
                device.startMeasuring()
            }
            timer.base = SystemClock.elapsedRealtime()
            timer.start()

            isRunning = true
            mainHandler.postDelayed(updatePredictionTask, 4000)
        } else {
            Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopDataCollection() {
        timer.stop()
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }

        isRunning = false
        mainHandler.removeCallbacks(updatePredictionTask)
    }

    private fun addPredictionViews(output: FloatArray) {
        predictions.clear()

        val outputLabelMap = mapOf(
            0 to "Running",
            1 to "Squats",
            2 to "Stairs Down",
            3 to "Stairs Up",
            4 to "Standing",
            5 to "Walking"
        ).withDefault { "" }

        for (i in output.indices) {
            val percentage = round(output[i] * 10000) / 100
            val prediction = Prediction(outputLabelMap[i]!!, percentage)
            predictions.add(prediction)
        }

        predictions.sortWith(Prediction.PredictionsComparator)

        adapter.notifyDataSetChanged()
    }

    private fun processAndPredict() {
        sensorDataByteBuffer = predictionHelper.processSensorData()
        predict()
    }

    private fun predict() {
        if (sensorDataByteBuffer == null) {
            Toast.makeText(
                context, "Please measure an activity first!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(
            intArrayOf(1, predictionHelper.dataVectorSize, predictionHelper.dataLineFloatSize),
            DataType.FLOAT32)
        inputFeature0.loadBuffer(sensorDataByteBuffer!!)

        // Runs model inference and gets result
        val outputs = predictionModel.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        // TODO: Do this when handler gets destroyed
        // predictionModel.close()

        addPredictionViews(outputFeature0.floatArray)
    }

    override fun activityCreated(activity: Activity) {

        this.activity = activity
        this.context = activity

        predictionHelper = PredictionHelper(context, rawSensorDataMap)

        // Initialising prediction RV
        recyclerView = activity.findViewById(R.id.rv_prediction)
        adapter = PredictionsAdapter(predictions)
        recyclerView.adapter = adapter

        // Initialising data array
        for ((_, address) in GlobalValues.sensorTagMap) {
            rawSensorDataMap[address] = mutableListOf<Pair<Long, FloatArray>>()
        }

        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        predictionButton = activity.findViewById(R.id.button_start_predict)

        predictionButton.setOnClickListener {
            togglePrediction()
        }

        predictionModel = Lstmmodel118.newInstance(context)
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {

        numConnectedDevices = devices.getConnected().size

        if (isRunning && numConnectedDevices < numDevices) {
            stopDataCollection()
            UIHelper.showAlert(context, "Connection to device(s) lost!")
        }
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {

        val timeStamp: Long = xsensDotData.sampleTimeFine
        val quat: FloatArray = xsensDotData.quat
        val freeAcc: FloatArray = xsensDotData.freeAcc

        rawSensorDataMap[deviceAddress]?.add(Pair(timeStamp, quat + freeAcc))
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // TODO("Not yet implemented")
    }
}
