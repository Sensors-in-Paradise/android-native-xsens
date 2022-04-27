package sensors_in_paradise.sonar.screen_train

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class TrainingHistoryAdapter(trainingHistory: ArrayList<TrainingHistoryStorage.TrainingOccasion>) :
    RecyclerView.Adapter<TrainingHistoryAdapter.ViewHolder>() {
    var trainingHistory = trainingHistory
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }
    private val dateFormat = DateFormat.getDateTimeInstance()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTV: TextView = view.findViewById(R.id.tv_title_trainingOccasion)
        val subTitleTV: TextView = view.findViewById(R.id.tv_subtitle_trainingOccasion)
        val upperSegmentView: View = view.findViewById(R.id.view_upperSegment_trainingOccasion)
        val lowerSegmentView: View = view.findViewById(R.id.view_lowerSegment_trainingOccasion)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.training_occasion, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val occasion = trainingHistory[position]
        holder.titleTV.text = dateFormat.format(Date(occasion.timestamp))
        var subtitle = "Duration ${GlobalValues.getDurationAsString(occasion.getTotalDuration())}"
        subtitle += "\n${occasion.getNumActivities()} activities"
        subtitle += "\n${occasion.getNumPeople()} people"
        holder.subTitleTV.text = subtitle

        if (position == itemCount - 1) {
            holder.lowerSegmentView.visibility = View.INVISIBLE
        }
        if (position == 0) {
            holder.upperSegmentView.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return trainingHistory.size
    }

    /* Use this function instead of notifyItemInserted:
        With this adapter, multiple adjacent items need be re-rendered when a new item is added,
        so that the time beam stays correct
    */
    fun notifyItemAdded(position: Int) {
        notifyItemInserted(position)
        if (itemCount >= 2) {
            when (position) {
                0 -> {
                    notifyItemChanged(position + 1)
                }
                itemCount - 1 -> {
                    notifyItemChanged(position - 1)
                }
            }
        }
    }
}
