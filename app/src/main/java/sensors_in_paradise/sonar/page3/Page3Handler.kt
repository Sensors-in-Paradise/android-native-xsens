package sensors_in_paradise.sonar.page3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.ViewSwitcher
import sensors_in_paradise.sonar.util.UIHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotPayload
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.util.PredictionHelper
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.XSENSArrayList
import sensors_in_paradise.sonar.ml.Lstmmodel118
import sensors_in_paradise.sonar.util.PreferencesHelper
import kotlin.collections.ArrayList
import java.nio.ByteBuffer
import kotlin.math.round

class Page3Handler(
    private val devices: XSENSArrayList,
    private val sensorOccupationInterface: SensorOccupationInterface?
) : PageInterface, ConnectionInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private lateinit var predictionButton: MaterialButton
    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var progressBar: ProgressBar
    private lateinit var timer: Chronometer

    private lateinit var metadataStorage: XSensDotMetadataStorage
    private lateinit var predictionHelper: PredictionHelper
    private val predictions = ArrayList<Prediction>()
    private val rawSensorDataMap = mutableMapOf<String, MutableList<Pair<Long, FloatArray>>>()
    // private var sensorDataByteBuffer: ByteBuffer? = null

    private var lastPrediction = 0L

    private val numDevices = 3
    private var numConnectedDevices = 0
    private var isRunning = false

    private lateinit var predictionModel: Lstmmodel118
    private lateinit var mainHandler: Handler

    private val predictionInterval = 4000L
    private val updatePredictionTask = object : Runnable {
        override fun run() {
            processAndPredict()
            mainHandler.postDelayed(this, predictionInterval)
        }
    }
    private val updateProgressBarTask = object : Runnable {
        override fun run() {
            var progress = 0

            if (isRunning) {
                progress =
                    ((100 * (System.currentTimeMillis() - lastPrediction)) / predictionInterval).toInt()
                mainHandler.postDelayed(this, 40)
            }
            progressBar.progress = progress
        }
    }

    private fun togglePrediction() {
        if (isRunning) {
            stopDataCollection()
        } else {
            startDataCollection()
        }
    }

    private fun clearBuffers() {
        rawSensorDataMap.clear()
    }

    private fun resetSensorDataLists() {
        for (key in rawSensorDataMap.keys) {
            rawSensorDataMap[key]?.clear()
        }
    }

    private fun startDataCollection() {
        sensorOccupationInterface?.onSensorOccupationStatusChanged(true)
        clearBuffers()
        lastPrediction = 0L
        if (tryInitializeSensorDataMap()) {
            for (device in devices.getConnected()) {
                device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
                device.startMeasuring()
            }
            timer.base = SystemClock.elapsedRealtime()
            timer.start()

            isRunning = true
            mainHandler.postDelayed(updatePredictionTask, 4000)
            mainHandler.postDelayed(updateProgressBarTask, 100)
            progressBar.visibility = View.VISIBLE
            predictionButton.setIconResource(R.drawable.ic_baseline_stop_24)
            viewSwitcher.displayedChild = 1
        }
    }

    private fun tryInitializeSensorDataMap(): Boolean {
        if (numConnectedDevices < numDevices) {
            Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            return false
        }
        val deviceSetKey =
            metadataStorage.tryGetDeviceSetKey(devices.getConnectedWithOfflineMetadata())
                ?: return false

        for (tagPrefix in GlobalValues.sensorTagPrefixes) {
            rawSensorDataMap[GlobalValues.formatTag(tagPrefix, deviceSetKey)] = mutableListOf()
        }
        return true
    }

    private fun stopDataCollection() {
        sensorOccupationInterface?.onSensorOccupationStatusChanged(false)
        timer.stop()
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }
        viewSwitcher.displayedChild = 0
        isRunning = false
        mainHandler.removeCallbacks(updatePredictionTask)
        mainHandler.removeCallbacks(updateProgressBarTask)
        progressBar.visibility = View.INVISIBLE
        predictionButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addPredictionViews(output: FloatArray) {
        predictions.clear()

        val outputLabelMap = mapOf(
            0 to "Non-fatigue",
            1 to "Fatigue"
        ).withDefault { "" }

        for (i in output.indices) {
            val percentage = round(output[i] * 10000) / 100
            val prediction = Prediction(outputLabelMap[i]!!, percentage)
            predictions.add(prediction)
        }

        predictions.sortWith(Prediction.PredictionsComparator)
        adapter.notifyDataSetChanged()
        viewSwitcher.displayedChild = 0
    }

    private fun processAndPredict() {
        val buffer = predictionHelper.processSensorData(rawSensorDataMap)
        if (buffer == null) {
            Toast.makeText(
                context, "Please measure an activity first!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            predict(buffer)
            resetSensorDataLists()
        }
    }

    private fun predict(sensorDataByteBuffer: ByteBuffer) {
        lastPrediction = System.currentTimeMillis()
        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(
            intArrayOf(1, predictionHelper.dataVectorSize, predictionHelper.dataLineFloatSize),
            DataType.FLOAT32
        )
        inputFeature0.loadBuffer(sensorDataByteBuffer)

        // Runs model inference and gets result
        val outputs = predictionModel.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        addPredictionViews(outputFeature0.floatArray)
    }

    override fun activityCreated(activity: Activity) {

        this.activity = activity
        this.context = activity

        metadataStorage = XSensDotMetadataStorage(context)
        predictionHelper =
            PredictionHelper(context, PreferencesHelper.shouldShowToastsVerbose(context))

        // Initializing prediction RV
        recyclerView = activity.findViewById(R.id.rv_prediction)
        adapter = PredictionsAdapter(predictions, activity.getColor(R.color.colorPrimary))
        recyclerView.adapter = adapter
        viewSwitcher = activity.findViewById(R.id.viewSwitcher_predictionFragment)
        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        predictionButton = activity.findViewById(R.id.button_start_predict)
        progressBar = activity.findViewById(R.id.progressBar_nextPrediction_predictionFragment)
        predictionButton.setOnClickListener {
            togglePrediction()
        }

        predictionModel = Lstmmodel118.newInstance(context)
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun activityResumed() {
    }

    override fun activityWillDestroy() {
        // Nothing to do
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
        val rps: DoubleArray = xsensDotData.gyr
        // convert rps to degrees per second
        val dps: FloatArray = rps.map { (it * 6 * 60).toFloat() }.toFloatArray()
        val freeAcc: FloatArray = xsensDotData.freeAcc

        val deviceTag = metadataStorage.getTagForAddress(deviceAddress)
        rawSensorDataMap[deviceTag]?.add(Pair(timeStamp, dps + freeAcc))
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // Nothing to do (?)
    }
}
