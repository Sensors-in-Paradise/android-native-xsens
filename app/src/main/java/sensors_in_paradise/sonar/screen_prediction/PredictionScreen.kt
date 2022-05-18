package sensors_in_paradise.sonar.screen_prediction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotPayload
import com.xsens.dot.android.sdk.utils.XsensDotParser
import org.tensorflow.lite.support.metadata.MetadataExtractor
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.screen_connection.ConnectionInterface
import sensors_in_paradise.sonar.use_cases.UseCase
import sensors_in_paradise.sonar.util.PredictionHelper
import sensors_in_paradise.sonar.util.PreferencesHelper
import sensors_in_paradise.sonar.util.UIHelper
import sensors_in_paradise.sonar.util.dialogs.MessageDialog
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.round
import kotlin.properties.Delegates

class PredictionScreen(
    private var currentUseCase: UseCase,
    private val devices: XSENSArrayList,
    private val sensorOccupationInterface: SensorOccupationInterface?
) : ScreenInterface, ConnectionInterface {

    private var featuresToPredict: ArrayList<String> = ArrayList()
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

    private var lastPrediction = 0L

    private var numDevices by Delegates.notNull<Int>()
    private var numConnectedDevices = 0
    private var isRunning = false

    private lateinit var mainHandler: Handler
    private val frequency = 60 // Hz
    private var predictionInterval = 4000L // ms
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
        lastPrediction = 0L
        if (tryInitializeSensorDataMap()) {
            for (device in devices.getConnected()) {
                device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_CUSTOM_MODE_4
                device.startMeasuring()
            }
            timer.base = SystemClock.elapsedRealtime()
            timer.start()

            isRunning = true
            mainHandler.postDelayed(updatePredictionTask, predictionInterval)
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
        model?.close()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addPredictionViews(output: FloatArray) {
        predictions.clear()

        val outputLabelMap = model!!.getLabelsMap()

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
        model?.predict(sensorDataByteBuffer)?.let { addPredictionViews(it) }
    }

    override fun onActivityCreated(activity: Activity) {

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

        mainHandler = Handler(Looper.getMainLooper())
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

        val deviceTag = metadataStorage.getTagForAddress(deviceAddress)
        val features = ArrayList<FloatArray>()
        featuresToPredict.forEach { s ->
            val dataProccessor = XsensDotParser.getDefaultDataProcessor()
            val packet = XsensDotParser.getXsDataPacket(dataProccessor, xsensDotData.dq, xsensDotData.dv)
            features.add(
                when (s) {
                    "Gyr" -> XsensDotParser.getCalibratedGyroscopeData(packet).map { it.toFloat() }.toFloatArray()
                    "DV" -> xsensDotData.dv
                    "Acc" -> XsensDotParser.getCalibratedAcceleration(packet)
                    "DQ" -> xsensDotData.dq
                    else -> {
                        throw kotlin.IllegalArgumentException("Unknown feature: $s")
                    }
                } as FloatArray
            )
        }
        val featuresArray = features.reduce(FloatArray::plus)
        rawSensorDataMap[deviceTag]?.add(Pair(timeStamp, featuresArray))
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
        return if (modelHasMetaData(currentUseCase.getModelFile())) {
            initMetadataModel(currentUseCase.getModelFile())
            true
        } else {
            initStdModel()
            true
        }
    }

    private fun initMetadataModel(modelFile: File) {
        model = TFLiteModel(modelFile)
        numDevices = model!!.getNumDevices()
        featuresToPredict = model!!.getSensorDataToPredict()
        predictionInterval = model!!.getPredictionInterval() + 1000L //ms
        predictionHelper.normalizationParams = model!!.normalizationParams
    }

    private fun modelHasMetaData(modelFile: File): Boolean {
        val buffer = ByteBuffer.allocate(modelFile.readBytes().size)
        buffer.put(modelFile.readBytes())
        buffer.rewind()
        return MetadataExtractor(buffer).hasMetadata()
    }

    private fun initStdModel() {
        numDevices = (5).apply { predictionHelper.numDevices = this }
        predictionHelper.numQuats = 4
        predictionHelper.numAccs = 3
        model = TFLiteModel(
            currentUseCase.getModelFile(), intArrayOf(
                1,
                predictionHelper.dataVectorSize,
                predictionHelper.calcDataLineByteSize()
            ), 6
        )
        featuresToPredict = arrayListOf(
            "DV",
            "DQ"
        )
    }
}
