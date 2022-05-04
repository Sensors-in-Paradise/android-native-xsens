package sensors_in_paradise.sonar.screen_train

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.widget.Button
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.ScreenInterface
import sensors_in_paradise.sonar.screen_recording.RecordingDataManager
import sensors_in_paradise.sonar.use_cases.UseCase

class TrainingScreen(
    private val recordingsManager: RecordingDataManager,
    private var currentUseCase: UseCase
) : ScreenInterface {
    private lateinit var activitiesPieChart: PieChart
    private lateinit var peoplePieChart: PieChart
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var historyRV: RecyclerView
    private lateinit var trainingHistoryAdapter: TrainingHistoryAdapter
    private lateinit var trainingHistoryStorage: TrainingHistoryStorage
    private lateinit var trainBtn: Button
    override fun onActivityCreated(activity: Activity) {
        this.activity = activity
        this.context = activity
        activitiesPieChart = activity.findViewById(R.id.pieChart_availableData_trainingFragment)
        peoplePieChart = activity.findViewById(R.id.pieChart_availableDataPeople_trainingFragment)
        historyRV = activity.findViewById(R.id.recyclerView_history_trainingFragment)
        trainingHistoryStorage = TrainingHistoryStorage(currentUseCase)
        trainingHistoryAdapter = TrainingHistoryAdapter(trainingHistoryStorage.getTrainingHistory())
        trainBtn = activity.findViewById(R.id.button_trainModel_trainingFragment)
        historyRV.adapter = trainingHistoryAdapter
        trainBtn.setOnClickListener {
            val item = trainingHistoryStorage.addTrainingOccasion(
                currentUseCase.getRecordingsSubDir().name,
                recordingsManager.getPeopleDurationsOfTrainableRecordings(),
                recordingsManager.getActivityDurationsOfTrainableRecordings()
            )
            trainingHistoryAdapter.trainingHistory.add(0,
               item
            )
            trainingHistoryAdapter.notifyItemAdded(0)
        }
    }

    override fun onScreenOpened() {
        populateAndAnimateCharts()
    }
    fun populateAndAnimateCharts() {
        populateDurationPieChart(
            activitiesPieChart,
            recordingsManager.getActivityDurationsOfTrainableRecordings()
        )
        populateDurationPieChart(
            peoplePieChart,
            recordingsManager.getPeopleDurationsOfTrainableRecordings()
        )
        activitiesPieChart.animateY(1400, Easing.EaseInOutQuad)
        peoplePieChart.animateY(1400, Easing.EaseInOutQuad)
    }

    private fun populateDurationPieChart(pieChart: PieChart, labelledDurations: Map<String, Long>) {
        val entries = ArrayList<PieEntry>()
        for ((activity, duration) in improveData(labelledDurations)) {
            entries.add(
                PieEntry(
                    duration.toFloat(),
                    activity
                )
            )
        }

        val dataSet = PieDataSet(entries, "Election Results")

        prepareDataset(dataSet)
        val data = PieData(dataSet)

        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return GlobalValues.getDurationAsString(value.toLong())
            }
        })
        data.setValueTextSize(8f)
        data.setValueTextColor(context.getColorResCompat(android.R.attr.textColorPrimary))
        data.setValueTypeface(Typeface.DEFAULT)

        pieChart.legend.isEnabled = false
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.animate()
    }

    private fun prepareDataset(dataSet: PieDataSet) {
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colors
    }

    private fun improveData(
        data: Map<String, Long>,
        smallItemPercentageThreshold: Float = 0.04f
    ): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        var sum = 0L
        for ((_, duration) in data) {
            sum += duration
        }
        for ((activity, duration) in data) {
            val label = if (duration >= sum * smallItemPercentageThreshold) activity else "other"
            result[label] = duration + (result[label] ?: 0L)
        }
        return result
    }

    override fun onUseCaseChanged(useCase: UseCase) {
        currentUseCase = useCase
        trainingHistoryStorage = TrainingHistoryStorage(currentUseCase)
        trainingHistoryAdapter.trainingHistory = trainingHistoryStorage.getTrainingHistory()
        populateAndAnimateCharts()
    }
}

@ColorInt
@SuppressLint("ResourceAsColor")
private fun Context.getColorResCompat(@AttrRes id: Int): Int {
    val resolvedAttr = TypedValue()
    theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}
