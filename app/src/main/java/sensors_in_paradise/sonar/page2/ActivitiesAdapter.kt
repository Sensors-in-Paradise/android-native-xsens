package sensors_in_paradise.sonar.page2


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R


class ActivitiesAdapter(private val activities: java.util.ArrayList<Pair<Long, String>>) :
    RecyclerView.Adapter<ActivitiesAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTV: TextView = view.findViewById(R.id.tv_activity_recordingActivityItem)
        val startTimeTV: TextView = view.findViewById(R.id.tv_startTime_recordingActivityItem)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recording_activity_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val activity = activities[position]
        viewHolder.activityTV.text = activity.second
        viewHolder.startTimeTV.text = getStartTimeAsString(activity)
    }

    override fun getItemCount() = activities.size

    private fun getStartTimeAsString(activity: Pair<Long, String>):String{
        // TODO: find out why Time is shown incorrectly (seconds grow to fast) - verifiy if this is an issue at all
        if(activities.size>0){
            val recordingStartTime = activities[0].first
            val activityStartTime = activity.first

            return GlobalValues.getDurationAsString(activityStartTime- recordingStartTime)

        }
        return "??:??"
    }

}
