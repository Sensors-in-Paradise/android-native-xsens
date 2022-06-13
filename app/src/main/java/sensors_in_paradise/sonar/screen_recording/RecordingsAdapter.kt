package sensors_in_paradise.sonar.screen_recording

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.SensorPlacementEstimator
import sensors_in_paradise.sonar.screen_recording.labels_editor.LabelsEditorDialog
import sensors_in_paradise.sonar.util.dialogs.MessageDialog
import sensors_in_paradise.sonar.util.dialogs.VideoDialog
import sensors_in_paradise.sonar.use_cases.UseCase
import java.text.DateFormat
import java.util.*

class RecordingsAdapter(
    private val recordings: RecordingDataManager,
    private val context: Context,
    var currentUseCase: UseCase
) :

    RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {
    private val dateFormat = DateFormat.getDateTimeInstance()

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
        val recording = recordings[position]
        val metadata = recording.metadataStorage

        val personName = metadata.getPerson()
        val duration = metadata.getDuration()
        val start = dateFormat.format(Date(metadata.getTimeStarted()))

        viewHolder.apply {
            deleteButton.setOnClickListener {
                showDeleteRecordingDialog(recording)
            }
             itemView.setOnClickListener {
                val onEditBtnClickListener =
                    DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                        LabelsEditorDialog(context, currentUseCase, recording) {
                            notifyItemChanged(position)
                        }
                    }
                val title = recording.getDisplayTitle() + " ($personName)"
                if (recording.hasVideoRecording()) {
                    MessageDialog(
                        context,
                        recording.getActivitiesSummary(),
                        title,
                        "Edit",
                        onEditBtnClickListener,
                        "Show video"
                    ) { _, _ -> VideoDialog(context, recording.getVideoFile()) }
                } else {
                    MessageDialog(
                        context,
                        recording.getActivitiesSummary(),
                        title = title,
                        "Edit",
                        onEditBtnClickListener
                    )
                }
            }
            itemView.setOnLongClickListener {
                SensorPlacementEstimator().tryEstimateSensorPlacements(context, recording)
                true
            }

            activityTextView.text =
                recording.getDisplayTitle()
            durationTextView.text = "Duration: " + GlobalValues.getDurationAsString(duration)
            startTimeTextView.text = "Start: $start"
            personTextView.text = "Person: $personName"

            // Set check file text & color conditionally
            checkFilesTextView.setTextColor(getCheckFileColor(recording))
            checkFilesTextView.text = getCheckFileText(recording)
        }
    }

    private fun getCheckFileText(recording: Recording): String {
        return when (recording.state) {
            RecordingFileState.WithoutSensor -> "No Sensor Data was collected"
            RecordingFileState.Empty -> "Some files are empty"
            RecordingFileState.Unsynchronized -> "Files are not synchronized"
            RecordingFileState.Valid -> "Files checked and synchronized"
        }
    }

    private fun getCheckFileColor(recording: Recording): Int {
        return ContextCompat.getColor(
            context,
            when (recording.state) {
                RecordingFileState.WithoutSensor -> R.color.orange
                RecordingFileState.Empty -> R.color.red
                RecordingFileState.Unsynchronized -> R.color.yellow
                RecordingFileState.Valid -> R.color.green
            }
        )
    }

    private fun showDeleteRecordingDialog(recording: Recording) {
        MessageDialog(
            context,
            "Do you really want to delete this recording?",
            onPositiveButtonClickListener = { _, _ ->
                val index = recordings.indexOf(recording)
                recordings.deleteRecording(recording)
                notifyItemRemoved(index)
            })
    }

    override fun getItemCount() = recordings.size
}
