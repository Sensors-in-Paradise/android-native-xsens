package sensors_in_paradise.sonar.page2

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.util.dialogs.MessageDialog
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.util.dialogs.VideoDialog
import java.text.DateFormat
import java.util.*

class RecordingsAdapter(
    private val recordingsManager: RecordingDataManager,
    private val context: Context
) :

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
        val videoView: VideoView = view.findViewById(R.id.videoView_videoCapture_recording)
        val textInfoLL: LinearLayout = view.findViewById(R.id.linearLayout_textInfo_recording)
        val videoViewFrameLayout: FrameLayout = view.findViewById(R.id.frameLayout_videoCapture_recording)
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
        val activitiesSummary =
            metadata.getActivities().joinToString("\n") { (activityStartTime, activity) ->
                GlobalValues.getDurationAsString(activityStartTime - metadata.getTimeStarted()) + "   " +
                        activity
            }
        val personName = metadata.getPerson()
        val duration = metadata.getDuration()
        val start = dateFormat.format(Date(metadata.getTimeStarted()))

        viewHolder.apply {
            deleteButton.setOnClickListener {
                val index = dataSet.indexOf(recording)
                recordingsManager.deleteRecording(recording)
                notifyItemRemoved(index)
            }
           itemView.setOnClickListener {
                MessageDialog(context, activitiesSummary)
            }
            activityTextView.text =
                recording.getDisplayTitle()
            durationTextView.text = "Duration: " + GlobalValues.getDurationAsString(duration)
            startTimeTextView.text = "Start: $start"
            personTextView.text = "Person: $personName"

            // Set check file text & color conditionally
            checkFilesTextView.setTextColor(getCheckFileColor(recording))
            checkFilesTextView.text = getCheckFileText(recording)
            if (recording.hasVideoRecording()) {
                videoViewFrameLayout.setOnClickListener {
                    VideoDialog(context, recording.getVideoFile())
                }
            }
            videoView.apply {
                if (recording.hasVideoRecording()) {
                    visibility = View.VISIBLE
                    setVideoPath(recording.getVideoFile().absolutePath)
                    setOnPreparedListener { mp -> mp.isLooping = true }
                    start()
                } else {
                    videoView.visibility = View.GONE
                }
            }
        }
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
