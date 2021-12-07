package sensors_in_paradise.sonar.page2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R
import java.io.File

class RecordingsAdapter(private val recordingsManager: RecordingFilesManager) :
    RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {
    private var dataSet: ArrayList<String> = recordingsManager.getRecordings()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTextView: TextView
        val durationTextView: TextView
        val startTimeTextView: TextView
        val deleteButton: Button

        init {
            activityTextView = view.findViewById(R.id.tv_activity)
            durationTextView = view.findViewById(R.id.tv_duration)
            startTimeTextView = view.findViewById(R.id.tv_start)
            deleteButton = view.findViewById(R.id.button_delete)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recording, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.deleteButton.setOnClickListener {
            recordingsManager.deleteRecording(File(dataSet[position]))
            update()
        }

        var activityName = recordingsManager.getActivityFromRecording(dataSet[position])
        var activityDuration = recordingsManager.getDurationFromRecording(dataSet[position])
        var activityStart = recordingsManager.getStartingTimeFromRecording(dataSet[position])
        viewHolder.activityTextView.text = activityName
        viewHolder.durationTextView.text = activityDuration
        viewHolder.startTimeTextView.text = activityStart
    }

    override fun getItemCount() = dataSet.size

    fun update() {
        dataSet = recordingsManager.getRecordings()
        notifyDataSetChanged()
    }

}
