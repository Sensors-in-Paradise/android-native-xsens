package sensors_in_paradise.sonar.page2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecordingsAdapter(private val recordingsManager: RecordingDataManager) :

    RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {
    private val dateFormat = DateFormat.getDateTimeInstance()
    private var dataSet: ArrayList<Recording> = recordingsManager.recordingsList

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTextView: TextView = view.findViewById(R.id.tv_activity)
        val personTextView: TextView = view.findViewById(R.id.tv_person)
        val durationTextView: TextView = view.findViewById(R.id.tv_duration)
        val startTimeTextView: TextView = view.findViewById(R.id.tv_start)
        val checkFilesTextView: TextView = view.findViewById(R.id.tv_check_files)
        val deleteButton: Button = view.findViewById(R.id.button_delete)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recording, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val recording = dataSet[position]
        val metadata = recording.metadataStorage
        val isValid = recording.areFilesValid
        viewHolder.deleteButton.setOnClickListener {
            val index = dataSet.indexOf(recording)
            recordingsManager.deleteRecording(recording)
            notifyItemRemoved(index)
        }

        val activityNames =
            metadata.getActivities().joinToString(", ") { (_, activity) -> activity }
        val personName = metadata.getPerson()
        val activityDuration = metadata.getDuration()

        val start = dateFormat.format(Date(metadata.getTimeStarted()))

        viewHolder.activityTextView.text = activityNames
        viewHolder.durationTextView.text = "Duration: " + GlobalValues.getDurationAsString(activityDuration)
        viewHolder.startTimeTextView.text = "Start: $start"
        viewHolder.personTextView.text = "Person: " + personName
        if (!isValid) {
            viewHolder.checkFilesTextView.setTextColor(Color.parseColor("#E53935"))
            viewHolder.checkFilesTextView.text = "Some files are empty"
        } else {
            viewHolder.checkFilesTextView.setTextColor(Color.parseColor("#4CAF50"))
            viewHolder.checkFilesTextView.text = "Files checked"
        }
    }

    override fun getItemCount() = dataSet.size
}
