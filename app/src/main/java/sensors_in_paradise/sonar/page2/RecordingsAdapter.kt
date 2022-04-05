package sensors_in_paradise.sonar.page2

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.MessageDialog
import sensors_in_paradise.sonar.R
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecordingsAdapter(private val recordingsManager: RecordingDataManager, private val context: Context) :

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val recording = dataSet[position]
        val metadata = recording.metadataStorage
        viewHolder.deleteButton.setOnClickListener {
            val index = dataSet.indexOf(recording)
            recordingsManager.deleteRecording(recording)
            notifyItemRemoved(index)
        }

        val activitiesSummary =
            metadata.getActivities().joinToString("\n") { (activityStartTime, activity) ->
                GlobalValues.getDurationAsString(activityStartTime - metadata.getTimeStarted()) + "   " +
                activity }
        viewHolder.itemView.setOnClickListener {
            MessageDialog(context, activitiesSummary)
        }
        val personName = metadata.getPerson()
        val duration = metadata.getDuration()

        val start = dateFormat.format(Date(metadata.getTimeStarted()))
        val numActivities = metadata.getActivities().size
        viewHolder.activityTextView.text = "$numActivities ${if (numActivities == 1) "activity" else "activities"}"
        viewHolder.durationTextView.text = "Duration: " + GlobalValues.getDurationAsString(duration)
        viewHolder.startTimeTextView.text = "Start: $start"
        viewHolder.personTextView.text = "Person: $personName"

        // Set check file text & color conditionally
        viewHolder.checkFilesTextView.setTextColor(getCheckFileColor(recording))
        viewHolder.checkFilesTextView.text = getCheckFileText(recording)
    }

    private fun getCheckFileText(recording: Recording): String {
        return when (recording.state) {
            RecordingFileState.Empty -> "Some files are empty"
            RecordingFileState.Unsynchronized -> "Files are not synchronized"
            RecordingFileState.Valid -> "Files checked and synchronized"
        }
    }

    private fun getCheckFileColor(recording: Recording): Int {
        return ContextCompat.getColor(
            context,
            when (recording.state) {
                RecordingFileState.Empty -> R.color.red
                RecordingFileState.Unsynchronized -> R.color.yellow
                RecordingFileState.Valid -> R.color.green
            }
        )
    }

    override fun getItemCount() = dataSet.size
}
