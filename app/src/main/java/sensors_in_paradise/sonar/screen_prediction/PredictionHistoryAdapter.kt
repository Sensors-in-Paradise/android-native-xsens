package sensors_in_paradise.sonar.screen_prediction

import android.annotation.SuppressLint
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.history.HistoryAdapter
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class PredictionHistoryAdapter(predictionHistory: ArrayList<Prediction>) :
HistoryAdapter() {
    var predictionHistory = predictionHistory
    @SuppressLint("NotifyDataSetChanged")
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    private val dateFormat = DateFormat.getDateTimeInstance()

    override fun getTitleOfItem(position: Int): String {
        val prediction = predictionHistory[position]
        return prediction.title
    }

    override fun getSubtitleOfItem(position: Int) : String {
        val prediction = predictionHistory[position]
        return prediction.percentageAsString()
    }

    override fun getItemCount(): Int {
        return predictionHistory.size
    }
}