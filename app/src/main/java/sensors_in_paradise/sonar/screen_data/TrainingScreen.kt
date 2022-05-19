package sensors_in_paradise.sonar.screen_data

import android.app.Activity
import android.content.Context
import android.widget.Button
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

class TrainingScreen(
    private val recordingsManager: RecordingDataManager,
    private var currentUseCase: UseCase
) : ScreenInterface {
    private lateinit var activitiesPieChart: PieChart
    private lateinit var activitiesPieChartPopulator: PieChartPopulator
    private lateinit var peoplePieChart: PieChart
    private lateinit var peoplePieChartPopulator: PieChartPopulator
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
        activitiesPieChartPopulator = PieChartPopulator(context, activitiesPieChart)
        peoplePieChart = activity.findViewById(R.id.pieChart_availableDataPeople_trainingFragment)
        peoplePieChartPopulator = PieChartPopulator(context, peoplePieChart)
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
            trainingHistoryAdapter.trainingHistory.add(
                0,
                item
            )
            trainingHistoryAdapter.notifyItemAdded(0)
            historyRV.scrollToPosition(0)
        }
        trainBtn.isEnabled = false

        activitiesPieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val pieEntry = e as PieEntry?
                peoplePieChartPopulator.populateAndAnimateChart(
                    recordingsManager.getPeopleDurationsOfTrainableRecordings(pieEntry?.label)
                )
                peoplePieChart.isHighlightPerTapEnabled = false
            }

            override fun onNothingSelected() {
                peoplePieChartPopulator.populateAndAnimateChart(
                    recordingsManager.getPeopleDurationsOfTrainableRecordings()

                )
                peoplePieChart.isHighlightPerTapEnabled = true
            }
        })
        peoplePieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val pieEntry = e as PieEntry?
                activitiesPieChartPopulator.populateAndAnimateChart(
                    recordingsManager.getActivityDurationsOfTrainableRecordings(pieEntry?.label)
                )
                activitiesPieChart.isHighlightPerTapEnabled = false
            }

            override fun onNothingSelected() {
                activitiesPieChartPopulator.populateAndAnimateChart(
                    recordingsManager.getActivityDurationsOfTrainableRecordings()
                )
                activitiesPieChart.isHighlightPerTapEnabled = true
            }
        })
    }

    override fun onScreenOpened() {
        populateAndAnimateCharts()
    }

    private fun populateAndAnimateCharts() {
        activitiesPieChartPopulator.populateAndAnimateChart(
            recordingsManager.getActivityDurationsOfTrainableRecordings()
        )
        peoplePieChartPopulator.populateAndAnimateChart(
            recordingsManager.getPeopleDurationsOfTrainableRecordings()
        )
    }

    override fun onUseCaseChanged(useCase: UseCase) {
        currentUseCase = useCase
        trainingHistoryStorage = TrainingHistoryStorage(currentUseCase)
        trainingHistoryAdapter.trainingHistory = trainingHistoryStorage.getTrainingHistory()
        populateAndAnimateCharts()
    }
}
