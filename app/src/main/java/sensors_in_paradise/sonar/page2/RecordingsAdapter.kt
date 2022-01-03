package sensors_in_paradise.sonar.page2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R
import java.io.File

class RecordingsAdapter(private val recordingsManager: RecordingDataManager) :
    RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {
    private var dataSet: ArrayList<String> = recordingsManager.getRecordings()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTextView: TextView
        val durationTextView: TextView
        val startTimeTextView: TextView
        val checkFilesTextView: TextView
        val deleteButton: Button

        init {
            activityTextView = view.findViewById(R.id.tv_activity)
            durationTextView = view.findViewById(R.id.tv_duration)
            startTimeTextView = view.findViewById(R.id.tv_start)
            checkFilesTextView = view.findViewById(R.id.tv_check_files)
            deleteButton = view.findViewById(R.id.button_delete)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recording, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val recording = dataSet[position]
        viewHolder.deleteButton.setOnClickListener {
            val index = dataSet.indexOf(recording)
            recordingsManager.deleteRecording(File(recording))
            notifyItemRemoved(index)
        }

        val filesEmpty = recordingsManager.checkEmptyFiles(File(recording))
        val activityName = recordingsManager.getActivityFromRecording(dataSet[position])
        val personName = recordingsManager.getPersonFromRecording(dataSet[position])
        val activityDuration = recordingsManager.getDurationFromRecording(dataSet[position])
        val activityStart = recordingsManager.getStartingTimeFromRecording(dataSet[position])
        viewHolder.activityTextView.text = activityName + " - " + personName
        viewHolder.durationTextView.text = "Duration: " + activityDuration
        viewHolder.startTimeTextView.text = "Start: " + activityStart

        if (filesEmpty) {
            viewHolder.checkFilesTextView.setTextColor(Color.parseColor("#E53935"))
            viewHolder.checkFilesTextView.text = "Some files are empty"
        } else {
            viewHolder.checkFilesTextView.setTextColor(Color.parseColor("#4CAF50"))
            viewHolder.checkFilesTextView.text = "Files checked"
        }
    }

    override fun getItemCount() = dataSet.size
}
