package sensors_in_paradise.sonar.page2.labels_editor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.constraintlayout.helper.widget.Carousel
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.slider.RangeSlider
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page2.PersistentCategoriesDialog
import sensors_in_paradise.sonar.page2.Recording

class LabelsEditorDialog(
    val context: Context,
    val recording: Recording,
    private val onRecordingChanged: () -> Unit
) : RangeSlider.OnSliderTouchListener {

    private var activitiesDialog: PersistentCategoriesDialog? = null
    private val editableRecording =
        EditableRecording(recording, this::onActivityInserted, this::onActivityRemoved)
    private val activities = editableRecording.activities
    private var carousel: Carousel
    private var motionLayout: MotionLayout
    private var previousItem: ClickableCarouselTextView
    private var currentItem: ClickableCarouselTextView
    private var nextItem: ClickableCarouselTextView
    private var endTV: TextView
    private var statusTV: TextView
    private var startTV: TextView
    private var visualizer: VisualSequenceViewHolder
    private var rangeSlider: RangeSlider
    var selectedItemIndex = 0
    private lateinit var neutralButton: Button

    init {
        val builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.label_editor, null)

        previousItem = root.findViewById(R.id.tv_carouselItem1_labelEditor)

        val videoView = root.findViewById<VideoView>(R.id.videoView_labelEditor)
        val poseSequenceView = root.findViewById<TextureView>(R.id.textureView_labelEditor)
        previousItem = root.findViewById(R.id.tv_carouselItem1_labelEditor)
        currentItem = root.findViewById(R.id.tv_carouselItem2_labelEditor)
        nextItem = root.findViewById(R.id.tv_carouselItem3_labelEditor)
        carousel = root.findViewById(R.id.carousel_labels_labelEditor)
        endTV = root.findViewById(R.id.tv_endDuration_labelEditor)
        startTV = root.findViewById(R.id.tv_startDuration_labelEditor)
        statusTV = root.findViewById(R.id.tv_consistencyStatus_labelEditor)
        rangeSlider = root.findViewById(R.id.rangeSlider_labelEditor)
        motionLayout = root.findViewById(R.id.motionLayout_carouselParent_labelEditor)
        rangeSlider.setLabelFormatter { value -> GlobalValues.getDurationAsString(value.toLong()) }
        val visualizerPreparingIndicator = root.findViewById<ProgressBar>(R.id.progressBar_visualizer_labelEditor)

        //val viewSwitcher = ViewSwitcher(context)
        val onPreparedListener = {visualizerPreparingIndicator.visibility = View.GONE}
        // TODO handle both together
        if (recording.hasVideoRecording()) {
            //viewSwitcher.addView(videoView)
            visualizer = VideoViewHolder(videoView, onPreparedListener)
            visualizer.sourcePath = recording.getVideoFile().absolutePath

            poseSequenceView.visibility = View.GONE
        } else {
            //viewSwitcher.addView(poseSequenceView)
            visualizer = PoseSequenceViewHolder(poseSequenceView, onPreparedListener)
            visualizer.sourcePath = recording.getPoseSequenceFile().absolutePath

            videoView.visibility = View.GONE
        }

        rangeSlider.addOnChangeListener { slider, value, _ ->
            val numThumbs = slider.values.size

            if (slider.activeThumbIndex == 0) {
                editableRecording.setRelativeStartTimeOfActivity(
                    selectedItemIndex,
                    value.toLong()
                )
            } else if (slider.activeThumbIndex == numThumbs - 1) {
                editableRecording.setRelativeEndTimeOfActivity(
                    selectedItemIndex,
                    value.toLong()
                )
            }
            visualizer.stopLooping()
            visualizer.seekTo(
                editableRecording.relativeSensorTimeToVideoTime(value.toLong())
            )
            Log.d("LabelsEditorDialog", "activeThumb: ${slider.activeThumbIndex}")
        }
        rangeSlider.addOnSliderTouchListener(this)

        notifyNewActivitySelected()

        carousel.setAdapter(object : Carousel.Adapter {
            override fun count(): Int {
                return activities.size
            }

            override fun populate(view: View, index: Int) {
                // need to implement this to populate the view at the given index
                val tf = view as TextView

                tf.text = formatLabel(activities[index].activity)
            }

            override fun onNewItem(index: Int) {
                // called when an item is set
                selectedItemIndex = index
                notifyNewActivitySelected()
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
            Log.d("LabelsEditorDialog-", "onClick currentITem")
            showActivitiesDialog()
        }

        builder.setView(root)
        builder.setPositiveButton(
            "Save"
        ) { _, _ ->
            editableRecording.save()
            onRecordingChanged()
        }
        builder.setNeutralButton(
            "Split", null
        )
        builder.setNegativeButton(
            "Cancel", null
        )

        val dialog = builder.create()
        dialog.setOnShowListener {
            neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                splitCurrentActivity()
            }
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setCancelable(false)
        }
        dialog.setOnDismissListener {
            visualizer.stopLooping()
        }
        dialog.show()
    }

    private fun formatLabel(text: String): String {
        var result = text
        if (result.length > 9) {
            result = if (' ' in text) {
                val index = text.indexOf(' ')
                text.substring(0, index + 1) + "\n" + text.substring(index + 1)
            } else {
                text.substring(0, text.length / 2) + "\n" + text.substring(text.length / 2)
            }
        }
        return result
    }

    private fun updateRangeSlider() {
        val rangeStart = editableRecording.getRelativeStartTimeOfActivity(selectedItemIndex - 1)
        rangeSlider.valueFrom = rangeStart.toFloat()
        val rangeEnd = editableRecording.getRelativeEndTimeOfActivity(selectedItemIndex + 1)
        rangeSlider.valueTo = rangeEnd.toFloat()
        Log.d(
            "LabelsEditorDialog",
            "setActivitySelected rangeStart $rangeStart and rangeEnd $rangeEnd"
        )

        rangeSlider.values = arrayListOf(
            editableRecording.getRelativeStartTimeOfActivity(selectedItemIndex).toFloat(),
            editableRecording.getRelativeEndTimeOfActivity(selectedItemIndex).toFloat()
        )
        startTV.text = GlobalValues.getDurationAsString(rangeStart)
        endTV.text = GlobalValues.getDurationAsString(rangeEnd)

        val startTime =
            editableRecording.getRelativeStartTimeOfActivity(selectedItemIndex).toFloat()
        val endTime = editableRecording.getRelativeEndTimeOfActivity(selectedItemIndex).toFloat()

        Log.d("LabelsEditorDialog", "setActivitySelected startTime $startTime and endTime $endTime")
        rangeSlider.values = arrayListOf(
            startTime,
            endTime
        )
    }
    private fun loopVisualizerForSelectedActivity() {
        visualizer.loopInterval(
            editableRecording.relativeSensorTimeToVideoTime(
                editableRecording.getRelativeStartTimeOfActivity(
                    selectedItemIndex
                )
            ),
            editableRecording.relativeSensorTimeToVideoTime(
                editableRecording.getRelativeEndTimeOfActivity(
                    selectedItemIndex
                )
            )
        )
    }
    private fun notifyNewActivitySelected() {
        updateRangeSlider()
        statusTV.text =
            if (editableRecording.areTimeStampsConsistent()) "" else "inconsistent timestamps"
        loopVisualizerForSelectedActivity()
    }

    private fun showActivitiesDialog() {
        if (activitiesDialog == null) {
            activitiesDialog = PersistentCategoriesDialog(
                context,
                "Select an activity label",
                GlobalValues.getActivityLabelsJSONFile(context),
                defaultItems = GlobalValues.DEFINED_ACTIVITIES,
                callback = { value ->
                    activities[selectedItemIndex].activity = value
                    carousel.refresh()
                },
            )
        }
        activitiesDialog?.show(true)
    }

    private fun onActivityInserted(index: Int) {
        Log.d(
            "LabelsEditorDialog",
            "onActivityInserted at index $index while selectedItemIndex is $selectedItemIndex"
        )
        if (index <= selectedItemIndex) {
            selectedItemIndex += 1
            carousel.jumpToIndex(selectedItemIndex)
        }
        carousel.refresh()
    }

    private fun onActivityRemoved(index: Int) {
        Log.d(
            "LabelsEditorDialog",
            "onActivityRemoved at index $index while selectedItemIndex is $selectedItemIndex"
        )
        assert(index != selectedItemIndex)
        if (index <= selectedItemIndex) {
            selectedItemIndex -= 1
            carousel.jumpToIndex(selectedItemIndex)
        }
        carousel.refresh()
    }

    @SuppressLint("RestrictedApi")
    override fun onStartTrackingTouch(slider: RangeSlider) {
    }

    @SuppressLint("RestrictedApi")
    override fun onStopTrackingTouch(slider: RangeSlider) {
        if (!isInSplittingMode()) {
            updateRangeSlider()
        }
        loopVisualizerForSelectedActivity()
    }

    private fun isInSplittingMode(): Boolean {
        return rangeSlider.values.size == 3
    }

    @SuppressLint("SetTextI18n")
    private fun splitCurrentActivity() {
        val values = rangeSlider.values
        if (isInSplittingMode()) {
            neutralButton.text = "Split"
            editableRecording.splitActivity(selectedItemIndex, rangeSlider.values[1].toLong())
            values.removeAt(1)
        } else {
            neutralButton.text = "Confirm"
            values.add(
                1,
                rangeSlider.values[0] + (rangeSlider.values[1] - rangeSlider.values[0]) / 2
            )
        }
        // Trigger UI update of rangeSlider
        rangeSlider.values = values
        Log.d("LabelsEditorDialog", "splitCurrentActivity values: ${values.joinToString()}")
    }
}
