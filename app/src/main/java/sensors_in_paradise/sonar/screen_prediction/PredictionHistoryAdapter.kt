package sensors_in_paradise.sonar.screen_prediction

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.history.HistoryAdapter
import sensors_in_paradise.sonar.screen_train.PredictionHistoryStorage.Prediction
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PredictionHistoryAdapter(
    val context: Context,
    predictionHistory: ArrayList<Pair<Prediction, Long>>
) :
    HistoryAdapter() {
    var predictionHistory = predictionHistory
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var numConsecutivePredictions = 0

    private fun formatTimestamp(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val minutesString = if (minutes < 10) " $minutes" else "$minutes"
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val secondsString = if (seconds < 10) "0$seconds" else "$seconds"
        return "$minutesString:$secondsString"
    }

    private fun getAveragePercentage(
        oldPercentage: Float,
        joinedPercentage: Float,
        numConsecutivePredictions: Int
    ): Float {
        return (oldPercentage * numConsecutivePredictions + joinedPercentage) / (numConsecutivePredictions + 1)
    }

    fun addPrediction(
        prediction: Prediction,
        relativeTime: Long,
        predictionInterval: Long,
        recyclerView: RecyclerView
    ) {
        val lastPrediction = predictionHistory.firstOrNull()?.first
        if (lastPrediction?.label == prediction.label) {
            lastPrediction.percentage = getAveragePercentage(
                lastPrediction.percentage,
                prediction.percentage,
                numConsecutivePredictions
            )
            notifyItemChanged(0)
            numConsecutivePredictions += 1
        } else {
            val adjustedTime = relativeTime - predictionInterval
            predictionHistory.add(0, Pair(prediction, adjustedTime))
            notifyItemAdded(0)
            recyclerView.scrollToPosition(0)
            numConsecutivePredictions = 1
        }
    }

    override fun getPrefixOfItem(position: Int): String? {
        val (_, timestamp) = predictionHistory[position]
        return formatTimestamp(timestamp)
    }

    override fun getTitleOfItem(position: Int): String {
        val (prediction, _) = predictionHistory[position]
        return prediction.label
    }

    override fun getSubtitleOfItem(position: Int): String {
        val (prediction, _) = predictionHistory[position]
        return "Ø ${prediction.percentageAsString()}"
    }

    override fun getItemCount(): Int {
        return predictionHistory.size
    }
}