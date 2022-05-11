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
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.button.MaterialButton
import com.xsens.dot.android.sdk.events.XsensDotData
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.screen_connection.ConnectionInterface
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
    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var progressBar: ProgressBar
    private lateinit var timer: Chronometer
    private lateinit var textView: TextView
    private lateinit var barChart: BarChart

    private lateinit var metadataStorage: XSensDotMetadataStorage
    private var predictionHistoryStorage: PredictionHistoryStorage? = null
    private lateinit var predictionHelper: PredictionHelper
    private val rawSensorDataMap = mutableMapOf<String, MutableList<Pair<Long, FloatArray>>>()

    private var lastPredictionTime = 0L

    private val numOutputs = 6
    private val numDevices = 5
    private var numConnectedDevices = 0
    private var isRunning = false

    private lateinit var mainHandler: Handler

    private val predictionInterval = 4000L
    private val updatePredictionTask = object : Runnable {
        override fun run() {
            addPredictionToHistory(getDummyPrediction())
            //processAndPredict()
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

    private fun getDummyPrediction(): FloatArray {
        val size = 6
        val randoms = FloatArray(size)
        for (i in 0 until size) {
            randoms[i] = Random.nextFloat() * (i.toFloat() + 1)
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

        clearChartData()

        predictionHistoryStorage =
            PredictionHistoryStorage(
                currentUseCase,
                System.currentTimeMillis(),
                PreferencesHelper.shouldStorePrediction(context)
            )
        predictionHistoryAdapter.predictionHistory = arrayListOf<Pair<Prediction, Long>>()

        isRunning = true
        mainHandler.postDelayed(updatePredictionTask, 4000)
        mainHandler.postDelayed(updateProgressBarTask, 100)
        progressBar.visibility = View.VISIBLE
        predictionButton.setIconResource(R.drawable.ic_baseline_stop_24)
        viewSwitcher.displayedChild = 1
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
        viewSwitcher.displayedChild = 0
        isRunning = false
        mainHandler.removeCallbacks(updatePredictionTask)
        mainHandler.removeCallbacks(updateProgressBarTask)
        progressBar.visibility = View.INVISIBLE
        predictionButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
        model?.close()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addPredictionToHistory(output: FloatArray) {
        val outputLabelMap = mapOf(
            0 to "Running",
            1 to "Squats",
            2 to "Stairs Down",
            3 to "Stairs Up",
            4 to "Standing",
            5 to "Walking"
        ).withDefault { "" }

        val predictions = ArrayList<Prediction>()
        for (i in output.indices) {
            val percentage = round(output[i] * 10000) / 100
            val prediction = Prediction(outputLabelMap[i]!!, percentage)
            predictions.add(prediction)
        }
        val predictionsUnordered = predictions.clone() as ArrayList<Prediction>
        predictions.sortWith(Prediction.PredictionsComparator)
        val highestPrediction = predictions[0]

        setBarChartData(predictionsUnordered, highestPrediction.label)


        textView.text = highestPrediction.label

        predictionHistoryStorage?.let {
            val relativeTime = it.addPrediction(highestPrediction)
            predictionHistoryAdapter.addPrediction(
                highestPrediction,
                relativeTime,
                predictionInterval,
                recyclerView
            )
            viewSwitcher.displayedChild = 0
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
        predictionHistoryAdapter = PredictionHistoryAdapter(
            context,
            predictionHistoryStorage?.getPredictionHistory()
                ?: arrayListOf<Pair<Prediction, Long>>()
        )
        recyclerView.adapter = predictionHistoryAdapter
        viewSwitcher = activity.findViewById(R.id.viewSwitcher_predictionFragment)
        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        textView = activity.findViewById(R.id.tv_predict_prediction)
        textView.visibility = View.GONE
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
        initializeBarChart()

        mainHandler = Handler(Looper.getMainLooper())
    }

    private fun initializeBarChart() {
        barChart = activity.findViewById(R.id.barChart_predict_predictions)

        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setTouchEnabled(false)

        // scaling can now only be done on x- and y-axis separately

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false)
        barChart.isDoubleTapToZoomEnabled = false

        barChart.setDrawGridBackground(false)

        //val xAxisFormatter: ValueFormatter = DayAxisValueFormatter(chart)

        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        //xAxis.typeface = tfLight
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day

        // xAxis.labelCount = 7
        //xAxis.setValueFormatter(xAxisFormatter)

        //val custom: IAxisValueFormatter = MyAxisValueFormatter()

        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false

        clearChartData()
//        val l: Legend = chart.getLegend()
//        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
//        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
//        l.orientation = Legend.LegendOrientation.HORIZONTAL
//        l.setDrawInside(false)
//        l.form = LegendForm.SQUARE
//        l.formSize = 9f
//        l.textSize = 11f
//        l.xEntrySpace = 4f

//        val mv = XYMarkerView(this, xAxisFormatter)
//        mv.setChartView(barChart) // For bounds control
//
//        chart.setMarker(mv)
    }

    private fun clearChartData() {
        setBarChartData(
            ArrayList((0..numOutputs).map { Prediction("", 0.1f) }),
            "_"
        )
    }

    private fun setBarChartData(predictions: ArrayList<Prediction>, highestPrediction: String) {
        val values = ArrayList(predictions.mapIndexed { i, prediction ->
            BarEntry(
                i.toFloat(),
                prediction.percentage
            )
        })

        val dataSet = BarDataSet(values, "")
        dataSet.colors = (predictions).map {
            if (it.label == highestPrediction) context.getColor(R.color.colorPrimaryDark)
            else context.getColor(R.color.colorAccent)
        }
        dataSet.setValueTextColors((predictions).map { context.getColor(R.color.hardBackgroundContrast) })

        val barData = BarData(dataSet)
        barData.setValueTextSize(10f);
//        barData.setValueTypeface(tfLight);
        barData.barWidth = 0.9f;

        barChart.setData(barData)
        barChart.animateY(((0.1) * predictionInterval).toInt())
//        barChart.animator.ani
//        barChart.animat
        //barChart.invalidate()
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
