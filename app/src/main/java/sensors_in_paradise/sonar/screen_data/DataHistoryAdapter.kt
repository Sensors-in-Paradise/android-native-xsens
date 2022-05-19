package sensors_in_paradise.sonar.screen_data

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

class DataHistoryAdapter(trainingHistory: ArrayList<DataHistoryStorage.TrainingOccasion>) :
    RecyclerView.Adapter<DataHistoryAdapter.ViewHolder>() {
    var trainingHistory = trainingHistory
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
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
            .inflate(R.layout.data_occasion, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val occasion = trainingHistory[position]
        holder.titleTV.text = dateFormat.format(Date(occasion.timestamp))
        var subtitle = "Duration ${GlobalValues.getDurationAsString(occasion.getTotalDuration())}"
        subtitle += "\n\uD83D\uDCC2 ${occasion.subdirectory}"
        subtitle += "\n${occasion.getNumActivities()} activities"
        subtitle += "\n${occasion.getNumPeople()} people"
        holder.apply {
            subTitleTV.text = subtitle
            lowerSegmentView.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
            upperSegmentView.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
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
