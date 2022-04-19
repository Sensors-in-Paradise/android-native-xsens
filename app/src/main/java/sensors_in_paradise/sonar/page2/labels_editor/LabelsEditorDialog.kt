package sensors_in_paradise.sonar.page2.labels_editor

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.VideoView
import androidx.constraintlayout.helper.widget.Carousel
import com.google.android.material.slider.RangeSlider
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page2.PersistentCategoriesDialog
import sensors_in_paradise.sonar.page2.Recording

class LabelsEditorDialog(
    val context: Context,
    val recording: Recording
) {
    private var activitiesDialog: PersistentCategoriesDialog? = null
    private val editableRecording = EditableRecording(recording)
    private val activities = editableRecording.activities
    private var carousel: Carousel
    private var previousItem: TextView
    private var currentItem: TextView
    private var nextItem: TextView
    private var endTV: TextView
    private var mediaPlayer: MediaPlayer? = null
    private var rangeSlider: RangeSlider
    var selectedItemIndex = 0

    init {
        val builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.label_editor, null)

        previousItem = root.findViewById(R.id.tv_carouselItem1_labelEditor)
        val videoView = root.findViewById<VideoView>(R.id.videoView_labelEditor)
        previousItem = root.findViewById(R.id.tv_carouselItem1_labelEditor)
        currentItem = root.findViewById(R.id.tv_carouselItem2_labelEditor)
        nextItem = root.findViewById(R.id.tv_carouselItem3_labelEditor)
        carousel = root.findViewById(R.id.carousel_labels_labelEditor)
        endTV = root.findViewById(R.id.tv_endDuration_labelEditor)
        rangeSlider = root.findViewById(R.id.rangeSlider_labelEditor)

        rangeSlider.valueFrom = 0f
        rangeSlider.valueTo = editableRecording.getDuration().toFloat()



        rangeSlider.setLabelFormatter { value -> GlobalValues.getDurationAsString(value.toLong()) }
        if (recording.hasVideoRecording()) {
            videoView.setVideoPath(recording.getVideoFile().absolutePath)
            videoView.setOnPreparedListener { mp ->
                mediaPlayer = mp
            }
        } else {
            videoView.visibility = View.GONE
        }
        endTV.text = GlobalValues.getDurationAsString(editableRecording.getDuration())
        setActivitySelected(0)

        carousel.setAdapter(object : Carousel.Adapter {
            override fun count(): Int {
                return activities.size
            }

            override fun populate(view: View, index: Int) {
                // need to implement this to populate the view at the given index
                val tf = view as TextView
                tf.text = activities[index].activity

            }

            override fun onNewItem(index: Int) {
                // called when an item is set
                selectedItemIndex = index
                setActivitySelected(selectedItemIndex)
                Log.d("LabelsEditorDialog", "onNewItem $index")

            }
        })
        previousItem.setOnClickListener {
            Log.d("LabelsEditorDialog", "previousItem clicked")
            carousel.transitionToIndex(selectedItemIndex - 1, 1000)
        }
        nextItem.setOnClickListener {
            Log.d("LabelsEditorDialog", "nextItem clicked")
            carousel.transitionToIndex(selectedItemIndex + 1, 1000)
        }
        currentItem.setOnClickListener {
            showActivitiesDialog()
        }
        builder.setView(root)
        builder.setPositiveButton(
            "Yes"
        ) { _, _ ->
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ ->
            // User cancelled the dialog
            dialog.cancel()
        }

        builder.create().show()
    }

    fun setActivitySelected(index: Int) {

        val startTime = editableRecording.getRelativeStartTimeOfActivity(index).toFloat()
        val endTime =   editableRecording.getRelativeEndTimeOfActivity(index).toFloat()

        Log.d("LabelsEditorDialog", "setActivitySelected startTime $startTime and endTime $endTime")
        rangeSlider.values = arrayListOf(
          startTime,
            endTime
        )
    }

    fun showActivitiesDialog() {
        if (activitiesDialog == null) {
            activitiesDialog = PersistentCategoriesDialog(
                context,
                "Select an activity label",
                GlobalValues.getActivityLabelsJSONFile(context),
                defaultItems = GlobalValues.DEFINED_ACTIVITIES,
                callback = { value ->
                    activities[selectedItemIndex].activity = value
                    currentItem.text = value
                },
            )
        }
        activitiesDialog?.show(true)
    }

}
