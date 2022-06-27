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
import com.xsens.dot.android.sdk.models.XsensDotPayload
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
import java.util.Collections.max
import kotlin.math.max
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

    private val numOutputs = 6
    val outputLabelMap = mapOf(
            0 to "Aufräumen",
            1 to "Aufwecken",
            2 to "Bett Machen",
            3 to "Dokumentation",
            4 to "Essen Reichen",
            5 to "Waschen",
            6 to "Haare Kämmen",
            7 to "Medikamente Stellen",
            8 to "Hautpflege",
            9 to "Rollstuhl Transfer",
            10 to "Umkleiden",
    ).withDefault { "" }

    private val predictionList = listOf(
        Pair(1, 1),
        Pair(10, 8),
        Pair(5, 26),
        Pair(8, 100),
        Pair(10, 15),
        Pair(6, 4),
        Pair(0, 100)
    )

    private lateinit var linearLayout: LinearLayout
    private var shouldDisplayFinalLabel = false
    private val finalLabel = 10
    private var predictionIterator = 0

    private fun getNextDummyPrediction(): FloatArray {
        predictionIterator += 1
        val size = outputLabelMap.size

        var tempIterator = 0
        val actualLabel =
            if (shouldDisplayFinalLabel) finalLabel
            else predictionList.find {
                tempIterator += it.second
                tempIterator >= predictionIterator
            }?.first ?: finalLabel

        val predictions = mutableListOf<Float>()
        for (i in 0 until size) {
            predictions.add(Random.nextFloat())
        }
        predictions[actualLabel] = max(predictions) + (Random.nextFloat()/2f) + 0.5f

        return predictions.map { it / predictions.sum() }.toFloatArray()
    }

    private val numDevices = 5
    private var numConnectedDevices = 0
    private var isRunning = false

    private lateinit var mainHandler: Handler

    private val predictionInterval = 4000L
    private val updatePredictionTask = object : Runnable {
        override fun run() {
            predict(getNextDummyPrediction())
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

        predictionIterator = 0
        shouldDisplayFinalLabel = false

        isRunning = true
        mainHandler.postDelayed(updateProgressBarTask, 100)
        mainHandler.postDelayed(updatePredictionTask, 4000)
        progressBar.visibility = View.VISIBLE
        predictionButton.setIconResource(R.drawable.ic_baseline_stop_24)

        toggleMotionLayout.transitionToEnd()
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

        toggleMotionLayout.transitionToStart()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePrediction(output: FloatArray) {
        val predictions = ArrayList<Prediction>()
        for (i in output.indices) {
            val percentage = round(output[i] * 10000) / 100
            val prediction = Prediction(outputLabelMap[i]!!, percentage)
            predictions.add(prediction)
        }
        predictions.sortByDescending { it.percentage }
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
        model?.predict(sensorDataByteBuffer)?.let { updatePrediction(it) }
    }

    private fun predict(predictions: FloatArray) {
        lastPredictionTime = System.currentTimeMillis()
        updatePrediction(predictions)
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
            if (true) {
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
        toggleMotionLayout =
            activity.findViewById(R.id.motionLayout_predictionToggling_predictionFragment)

        mainHandler = Handler(Looper.getMainLooper())

        linearLayout = activity.findViewById(R.id.linearLayout_prediction)
        linearLayout.setOnLongClickListener {
            shouldDisplayFinalLabel = !shouldDisplayFinalLabel
            predictionIterator = 150
            true
        }
    }

    override fun onActivityResumed() {}

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
        // Nothing to do
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
