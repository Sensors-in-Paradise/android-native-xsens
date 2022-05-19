package sensors_in_paradise.sonar.screen_data

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.ScreenInterface
import sensors_in_paradise.sonar.screen_recording.RecordingDataManager
import sensors_in_paradise.sonar.use_cases.UseCase

class DataScreen(
    private val recordingsManager: RecordingDataManager,
    private var currentUseCase: UseCase
) : ScreenInterface {
    private lateinit var activitiesPieChart: PieChart
    private lateinit var activitiesPieChartPopulator: PieChartPopulator
    private lateinit var peoplePieChart: PieChart
    private lateinit var peoplePieChartPopulator: PieChartPopulator
    private lateinit var filterSwitch: SwitchCompat
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var historyTV: TextView
    private lateinit var historyRV: RecyclerView
    private lateinit var dataHistoryAdapter: DataHistoryAdapter
    private lateinit var dataHistoryStorage: DataHistoryStorage
    private lateinit var trainBtn: Button

    override fun onActivityCreated(activity: Activity) {
        this.activity = activity
        this.context = activity
        activitiesPieChart = activity.findViewById(R.id.pieChart_availableData_dataFragment)
        activitiesPieChartPopulator = PieChartPopulator(context, activitiesPieChart)
        peoplePieChart = activity.findViewById(R.id.pieChart_availableDataPeople_dataFragment)
        peoplePieChartPopulator = PieChartPopulator(context, peoplePieChart)
        filterSwitch = activity.findViewById(R.id.switch_filterForTraining_dataFragment)
        historyTV = activity.findViewById(R.id.textView_historyHeading_dataFragment)
        historyRV = activity.findViewById(R.id.recyclerView_history_dataFragment)
        dataHistoryStorage = DataHistoryStorage(currentUseCase)
        dataHistoryAdapter = DataHistoryAdapter(dataHistoryStorage.getTrainingHistory())
        trainBtn = activity.findViewById(R.id.button_trainModel_dataFragment)
        historyRV.adapter = dataHistoryAdapter
        trainBtn.setOnClickListener {
            val item = dataHistoryStorage.addTrainingOccasion(
                currentUseCase.getRecordingsSubDir().name,
                recordingsManager.getPeopleDurationsOfTrainableRecordings(),
                recordingsManager.getActivityDurationsOfTrainableRecordings()
            )
            dataHistoryAdapter.trainingHistory.add(
                0,
                item
            )
            dataHistoryAdapter.notifyItemAdded(0)
            historyRV.scrollToPosition(0)
            historyTV.isVisible = true
        }
        trainBtn.isEnabled = false

        activitiesPieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val pieEntry = e as PieEntry?
                peoplePieChartPopulator.populateAndAnimateChart(
                    if (filterSwitch.isChecked) {
                        recordingsManager.getPeopleDurationsOfTrainableRecordings(pieEntry?.label)
                    } else {
                        recordingsManager.getPeopleDurationsOfAllRecordings(pieEntry?.label)
                    }
                )
                peoplePieChart.isHighlightPerTapEnabled = false
            }

            override fun onNothingSelected() {
                peoplePieChartPopulator.populateAndAnimateChart(
                    if (filterSwitch.isChecked) {
                        recordingsManager.getPeopleDurationsOfTrainableRecordings()
                    } else {
                        recordingsManager.getPeopleDurationsOfAllRecordings()
                    }

                )
                peoplePieChart.isHighlightPerTapEnabled = true
            }
        })
        activitiesPieChart.description.isEnabled = false;

        peoplePieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val pieEntry = e as PieEntry?
                activitiesPieChartPopulator.populateAndAnimateChart(
                    if (filterSwitch.isChecked) {
                        recordingsManager.getActivityDurationsOfTrainableRecordings(pieEntry?.label)
                    } else {
                        recordingsManager.getActivityDurationsOfAllRecordings(pieEntry?.label)
                    }
                )
                activitiesPieChart.isHighlightPerTapEnabled = false
            }

            override fun onNothingSelected() {
                activitiesPieChartPopulator.populateAndAnimateChart(
                    if (filterSwitch.isChecked) {
                        recordingsManager.getActivityDurationsOfTrainableRecordings()
                    } else {
                        recordingsManager.getActivityDurationsOfAllRecordings()
                    }
                )
                activitiesPieChart.isHighlightPerTapEnabled = true
            }
        })
        peoplePieChart.description.isEnabled = false

        filterSwitch.setOnCheckedChangeListener { _, _ -> populateAndAnimateCharts() }
    }

    override fun onScreenOpened() {
        populateAndAnimateCharts()
    }

    private fun populateAndAnimateCharts() {
        activitiesPieChartPopulator.populateAndAnimateChart(
            if (filterSwitch.isChecked) {
                recordingsManager.getActivityDurationsOfTrainableRecordings()
            } else {
                recordingsManager.getActivityDurationsOfAllRecordings()
            }
        )
        peoplePieChartPopulator.populateAndAnimateChart(
            if (filterSwitch.isChecked) {
                recordingsManager.getPeopleDurationsOfTrainableRecordings()
            } else {
                recordingsManager.getPeopleDurationsOfAllRecordings()
            }
        )
    }

    override fun onUseCaseChanged(useCase: UseCase) {
        currentUseCase = useCase
        dataHistoryStorage = DataHistoryStorage(currentUseCase)
        dataHistoryAdapter.trainingHistory = dataHistoryStorage.getTrainingHistory()
        populateAndAnimateCharts()
    }
}
