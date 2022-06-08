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
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.screen_connection.ConnectionInterface
import sensors_in_paradise.sonar.use_cases.UseCase
import sensors_in_paradise.sonar.util.UIHelper
import sensors_in_paradise.sonar.util.dialogs.MessageDialog
import java.io.File
import kotlin.math.round

class PredictionScreen(
    private var currentUseCase: UseCase,
    private val devices: XSENSArrayList,
    private val sensorOccupationInterface: SensorOccupationInterface?
) : ScreenInterface, ConnectionInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private lateinit var predictionButton: MaterialButton
    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var progressBar: ProgressBar
    private lateinit var timer: Chronometer

    private lateinit var metadataStorage: XSensDotMetadataStorage
    private val predictions = ArrayList<Prediction>()

    private var lastPrediction = 0L

    private var numConnectedDevices = 0
    private var isRunning = false
    private var window: InMemoryWindow? = null
    private lateinit var mainHandler: Handler
    private var predictionInterval: Long? = null

    private val updatePredictionTask = object : Runnable {
        override fun run() {
            predict()
            mainHandler.postDelayed(this, predictionInterval!!)
        }
    }

    private val updateProgressBarTask = object : Runnable {
        override fun run() {
            var progress = 0
            if (isRunning) {
                progress =
                    ((100 * (System.currentTimeMillis() - lastPrediction)) / predictionInterval!!).toInt()
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

    private fun startDataCollection() {
        val requiredButNotConnectedDevices = getRequiredButNotConnectedDevices()
        if (requiredButNotConnectedDevices.isNotEmpty()) {
            UIHelper.showAlert(
                context = context,
                message = "The model for this use case requires sensors with the following tag prefixes to be connected:\n" +
                        "    ${model!!.getDeviceTags().joinToString("\n    ")}\n" +
                        "These are currently not connected:\n" +
                        "    ${requiredButNotConnectedDevices.joinToString("\n    ")}",
                title = "Not enough sensors connected"
            )
            return
        }
        sensorOccupationInterface?.onSensorOccupationStatusChanged(true)
        lastPrediction = 0L
        window = model?.let {
            InMemoryWindow(
                windowSize = it.windowSize,
                features = it.getFeaturesToPredict()
            )
        }
        for (device in devices.getConnected()) {
            device.measurementMode =
                XsensDotPayload.PAYLOAD_TYPE_CUSTOM_MODE_4 //TODO("Set measurement mode from model metadata")
            device.startMeasuring()
            timer.base = SystemClock.elapsedRealtime()
            timer.start()

            isRunning = true
            mainHandler.postDelayed(updatePredictionTask, predictionInterval!!)
            mainHandler.postDelayed(updateProgressBarTask, 100)
            progressBar.visibility = View.VISIBLE
            predictionButton.setIconResource(R.drawable.ic_baseline_stop_24)
            viewSwitcher.displayedChild = 1
        }
    }

    private fun getRequiredButNotConnectedDevices(): List<String> {
        val connectedDeviceTagPrefixes =
            devices.getConnectedWithOfflineMetadata().map { it.getTagPrefix() }
        val requiredDeviceTagPrefixes = model!!.getDeviceTags()
        return requiredDeviceTagPrefixes.filter { !connectedDeviceTagPrefixes.contains(it) }

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

    private fun predict() {
        lastPrediction = System.currentTimeMillis()
        if (window != null) {
            if (window!!.hasEnoughDataToCompileWindow()) {
                model?.predict(window!!.compileWindow())?.let { addPredictionViews(it) }
                window!!.clearValues()
            }
        }
    }

    override fun onActivityCreated(activity: Activity) {

        this.activity = activity
        this.context = activity

        metadataStorage = XSensDotMetadataStorage(context)
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

        if (isRunning && numConnectedDevices < model!!.getNumDevices()) {
            stopDataCollection()
            UIHelper.showAlert(context, "Connection to device(s) lost!")
        }
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        val deviceTag = metadataStorage.getTagForAddress(deviceAddress)
        val deviceTagPrefix = XSensDotDeviceWithOfflineMetadata.extractTagPrefixFromTag(deviceTag)
        if (deviceTagPrefix != null) {
            window?.appendSensorData(deviceTagPrefix, xsensDotData)
        } else {
            togglePrediction()
            activity.runOnUiThread {
                Toast.makeText(
                    context,
                    "Could not extract prefixe for device tag $deviceTag",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // Nothing to do
    }

    override fun onUseCaseChanged(useCase: UseCase) {
        currentUseCase = useCase
        initModelFromCurrentUseCase()
    }

    private fun initModelFromCurrentUseCase(): Boolean {
        if (!currentUseCase.getModelFile().exists()) {
            return false
        }
        initMetadataModel(currentUseCase.getModelFile())
        return true
    }

    private fun initMetadataModel(modelFile: File) {
        model = TFLiteModel(modelFile)
        predictionInterval = model!!.getWindowInSeconds() + cushion //ms
    }


    companion object {
        const val cushion = 1000L //ms
    }
}
