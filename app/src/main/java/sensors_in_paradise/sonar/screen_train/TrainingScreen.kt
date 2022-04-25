package sensors_in_paradise.sonar.screen_train

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
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

class TrainingScreen(private val recordingsManager: RecordingDataManager) : ScreenInterface {
    private lateinit var activitiesPieChart: PieChart
    private lateinit var peoplePieChart: PieChart
    private lateinit var context: Context
    private lateinit var activity: Activity
    override fun onActivityCreated(activity: Activity) {
        this.activity = activity
        this.context = activity
        activitiesPieChart = activity.findViewById(R.id.pieChart_availableData_trainingFragment)
        peoplePieChart = activity.findViewById(R.id.pieChart_availableDataPeople_trainingFragment)
    }

    override fun onScreenOpened() {
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
        // dataSet.setSelectionShift(0f);

        // dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)

        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return GlobalValues.getDurationAsString(value.toLong())
            }
        })
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(Typeface.DEFAULT)
        pieChart.legend.isEnabled = false
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.data = data

        // undo all highlights

        // undo all highlights
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
}
