package sensors_in_paradise.sonar.screen_prediction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.screen_connection.ConnectionInterface
import sensors_in_paradise.sonar.screen_prediction.barChart.PredictionBarChart
import sensors_in_paradise.sonar.screen_train.PredictionHistoryStorage
import sensors_in_paradise.sonar.screen_train.PredictionHistoryStorage.Prediction
import sensors_in_paradise.sonar.use_cases.UseCase
import sensors_in_paradise.sonar.util.PredictionHelper
import sensors_in_paradise.sonar.util.PreferencesHelper
import sensors_in_paradise.sonar.util.UIHelper
import sensors_in_paradise.sonar.util.dialogs.MessageDialog
import java.nio.ByteBuffer
import kotlin.math.round
import kotlin.random.Random


class PredictionScreen(
    private var currentUseCase: UseCase,
    private val devices: XSENSArrayList,
    private val sensorOccupationInterface: SensorOccupationInterface?
) : ScreenInterface, ConnectionInterface {

    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var predictionHistoryAdapter: PredictionHistoryAdapter
    private lateinit var predictionButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var timer: Chronometer
    private lateinit var textView: TextView
    private lateinit var predictionBarChart: PredictionBarChart

    private lateinit var toggleMotionLayout: MotionLayout
    private lateinit var metadataStorage: XSensDotMetadataStorage
    private var predictionHistoryStorage: PredictionHistoryStorage? = null
    private lateinit var predictionHelper: PredictionHelper
    private val rawSensorDataMap = mutableMapOf<String, MutableList<Pair<Long, FloatArray>>>()

    private var lastPredictionTime = 0L

    private val numOutputs = 7
    val outputLabelMap = mapOf(
        0 to "Running",
        1 to "Squats",
        2 to "Stairs Down",
        3 to "1 min Pitch",
        4 to "Standing",
        5 to "Walking",
        6 to "cooking"
    ).withDefault { "" }

    private val numDevices = 5
    private var numConnectedDevices = 0
    private var isRunning = false

    private lateinit var mainHandler: Handler

    private val predictionInterval = 4000L
    private val updatePredictionTask = object : Runnable {
        override fun run() {
            addPredictionToHistory(getDummyPrediction())
            // processAndPredict()
            mainHandler.postDelayed(this, predictionInterval)
        }
    }
    private val updateProgressBarTask = object : Runnable {
        override fun run() {
            var progress = 0

            if (isRunning) {
                progress =
                    ((100 * (System.currentTimeMillis() - lastPredictionTime)) / predictionInterval).toInt()
                mainHandler.postDelayed(this, 40)
            }
            progressBar.progress = progress
        }
    }
    private var model: TFLiteModel? = null

    private fun togglePrediction() {
        if (isRunning) {
            stopDataCollection()
            toggleMotionLayout.transitionToStart()
        } else {
            startDataCollection()
            toggleMotionLayout.transitionToEnd()
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

    private fun getDummyPrediction(): FloatArray {
        val randoms = FloatArray(numOutputs)
        for (i in 0 until numOutputs) {
            randoms[i] = Random.nextFloat() * (i.toFloat() + 1f) * (i.toFloat() + 1f) * (i.toFloat() + 1f)
        }
        return randoms.map { it / randoms.sum() }.toFloatArray()
    }

    private fun startDataCollection() {
        sensorOccupationInterface?.onSensorOccupationStatusChanged(true)
        clearBuffers()
        lastPredictionTime = 0L
//        if (tryInitializeSensorDataMap()) {
//            for (device in devices.getConnected()) {
//                device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
//                device.startMeasuring()
//            }
        timer.base = SystemClock.elapsedRealtime()
        timer.start()
        textView.visibility = View.VISIBLE
        textView.text = ""

        predictionBarChart.resetData()

        predictionHistoryStorage =
            PredictionHistoryStorage(
                currentUseCase,
                System.currentTimeMillis(),
                PreferencesHelper.shouldStorePrediction(context)
            )
        predictionHistoryAdapter.predictionHistory = arrayListOf()
        predictionHistoryAdapter.addPrediction(Prediction("", 0f), 0)

        isRunning = true
        mainHandler.postDelayed(updatePredictionTask, 4000)
        mainHandler.postDelayed(updateProgressBarTask, 100)
        progressBar.visibility = View.VISIBLE
        predictionButton.setIconResource(R.drawable.ic_baseline_stop_24)
//        }
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
        textView.text = ""
        textView.visibility = View.GONE
        isRunning = false
        mainHandler.removeCallbacks(updatePredictionTask)
        mainHandler.removeCallbacks(updateProgressBarTask)
        progressBar.visibility = View.INVISIBLE
        predictionButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
        model?.close()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addPredictionToHistory(output: FloatArray) {
        val predictions = ArrayList<Prediction>()
        for (i in output.indices) {
            val percentage = round(output[i] * 10000) / 100
            val prediction = Prediction(outputLabelMap[i]!!, percentage)
            predictions.add(prediction)
        }
        predictions.sortWith(Prediction.PredictionsComparator)
        val highestPrediction = predictions[0]

        predictionBarChart.changeData(predictions, highestPrediction.label)

        textView.text = highestPrediction.label

        predictionHistoryStorage?.let {
            val relativeTime = it.addPrediction(highestPrediction)
            predictionHistoryAdapter.addPrediction(
                highestPrediction,
                relativeTime,
                recyclerView
            )
        }
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
        lastPredictionTime = System.currentTimeMillis()
        model?.predict(sensorDataByteBuffer)?.let { addPredictionToHistory(it) }
    }

    override fun onActivityCreated(activity: Activity) {

        this.activity = activity
        this.context = activity

        metadataStorage = XSensDotMetadataStorage(context)
        predictionHelper =
            PredictionHelper(context, PreferencesHelper.shouldShowToastsVerbose(context))

        // Initializing prediction RV
        recyclerView = activity.findViewById(R.id.rv_prediction)
        predictionHistoryAdapter = PredictionHistoryAdapter(context, arrayListOf())
        recyclerView.adapter = predictionHistoryAdapter

        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        textView = activity.findViewById(R.id.tv_predict_prediction)
        predictionButton = activity.findViewById(R.id.button_start_predict)
        progressBar = activity.findViewById(R.id.progressBar_nextPrediction_predictionFragment)

        predictionButton.setOnClickListener {
            if (initModelFromCurrentUseCase()) {
                val signatures = model?.signatureKeys
                Log.d("PredictionScreen-onActivityCreated", signatures.toString())
                togglePrediction()
            } else {
                MessageDialog(
                    context,
                    message = context.getString(
                        R.string.missing_model_dialog_message,
                        currentUseCase.getModelFile().absolutePath
                    ),
                    title = context.getString(R.string.missing_model_dialog_title),
                    positiveButtonText = "Okay",
                    neutralButtonText = "Import default Model",
                    onNeutralButtonClickListener = { _, _ ->
                        currentUseCase.importDefaultModel()
                        initModelFromCurrentUseCase()
                        togglePrediction()
                    }
                )
            }
        }
        val barChart: BarChart = activity.findViewById(R.id.barChart_predict_predictions)
        predictionBarChart =
            PredictionBarChart(context, barChart, numOutputs, predictionInterval)
        toggleMotionLayout = activity.findViewById(R.id.motionLayout_predictionToggling_predictionFragment)

        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onActivityResumed() {
    }

    override fun onActivityWillDestroy() {
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
        val quat: FloatArray = xsensDotData.quat
        val freeAcc: FloatArray = xsensDotData.freeAcc

        val deviceTag = metadataStorage.getTagForAddress(deviceAddress)
        rawSensorDataMap[deviceTag]?.add(Pair(timeStamp, quat + freeAcc))
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // Nothing to do (?)
    }

    override fun onUseCaseChanged(useCase: UseCase) {
        currentUseCase = useCase
    }

    private fun initModelFromCurrentUseCase(): Boolean {
        if (!currentUseCase.getModelFile().exists()) {
            return false
        }
        model = TFLiteModel(
            currentUseCase.getModelFile(), intArrayOf(
                1,
                predictionHelper.dataVectorSize,
                predictionHelper.dataLineFloatSize
            ), 6
        )
        return true
    }
}
