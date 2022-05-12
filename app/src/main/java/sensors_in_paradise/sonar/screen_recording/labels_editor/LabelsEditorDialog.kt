package sensors_in_paradise.sonar.page2.labels_editor

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.TextureView
import android.view.LayoutInflater
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.*
import androidx.constraintlayout.helper.widget.Carousel
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.slider.RangeSlider
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.screen_recording.PersistentCategoriesDialog
import sensors_in_paradise.sonar.screen_recording.Recording
import sensors_in_paradise.sonar.screen_recording.labels_editor.*
import sensors_in_paradise.sonar.use_cases.UseCase
import sensors_in_paradise.sonar.util.PreferencesHelper
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class LabelsEditorDialog(
    val context: Context,
    val useCase: UseCase,
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

    /*Stores a list of VisualSequenceViewHolder and its position in the viewSwitcher*/
    private var visualizers: ArrayList<Pair<Int, VisualSequenceViewHolder>> = ArrayList()

    private var rangeSlider: RangeSlider
    private var selectedItemIndex = 0
    private var activeVisualizerIndex: Int? = null
    private lateinit var neutralButton: Button
    private var visualizerPreparingIndicator: ProgressBar
    private var visualizerSwitcher: ViewSwitcher
    private var visualizerFrameLayout: FrameLayout
    private var videoView: VideoView
    private var poseSequenceView: TextureView
    private var poseSequenceBackground: ImageView
    private val activeVisualizer: VisualSequenceViewHolder?
        get() {
            return if (activeVisualizerIndex != null) {
                visualizers[activeVisualizerIndex!!].second
            } else null
        }

    init {
        val builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.label_editor, null)

        previousItem = root.findViewById(R.id.tv_carouselItem1_labelEditor)

        videoView = root.findViewById(R.id.videoView_stickmanBackground_labelEditor)
        poseSequenceView = root.findViewById(R.id.textureView_stickmanBackground_labelEditor)
        poseSequenceBackground = root.findViewById(R.id.imageView_stickmanBackground_labelEditor)
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
        visualizerPreparingIndicator = root.findViewById(R.id.progressBar_visualizer_labelEditor)
        visualizerSwitcher = root.findViewById(R.id.viewSwitcher_visualizer_labelEditor)
        visualizerFrameLayout = root.findViewById(R.id.frameLayout_labelEditor)

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
            activeVisualizer?.stopLooping()
            activeVisualizer?.seekTo(
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
                startOrConfirmSplit()
            }
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            dialog.setCancelable(false)
            initVisualizers()
        }
        dialog.setOnDismissListener {
            activeVisualizer?.stopLooping()
        }
        dialog.show()
    }

    private fun initVisualizers() {
        if (recording.hasVideoRecording()) {
            val visualizer = VideoViewHolder(
                videoView,
                this::onVisualizerSourceLoaded,
                this::onVisualizerStartLoadingSource
            )
            visualizer.sourcePath = recording.getVideoFile().absolutePath
            visualizers.add(Pair(0, visualizer))
        }
        if (recording.hasPoseSequenceRecording()) {
            val visualizer = PoseSequenceViewHolder(
                context,
                poseSequenceView,
                this::onVisualizerSourceLoaded,
                this::onVisualizerStartLoadingSource
            )
            visualizer.sourcePath = recording.getPoseSequenceFile().absolutePath
            visualizers.add(Pair(1, visualizer))
        }
        if (visualizers.isNotEmpty()) {
            setActiveVisualizer(0, visualizers[0].first)
            if (visualizers.size > 1) {
                val horizontalSwipeDetector =
                    GestureDetectorCompat(context, HorizontalSwipeDetector { isSwipeToLeft ->
                        if (isSwipeToLeft && activeVisualizerIndex == 0) {
                            setActiveVisualizer(1, 1)
                        } else if (!isSwipeToLeft && activeVisualizerIndex == 1) {
                            setActiveVisualizer(0, 0)
                        }
                    })
                visualizerSwitcher.setOnTouchListener { _, event ->
                    horizontalSwipeDetector.onTouchEvent(event)
                }
            }
        } else {
            visualizerFrameLayout.visibility = View.GONE
        }
    }

    private fun setActiveVisualizer(visualizerIndex: Int, viewIndex: Int) {
        if (visualizerIndex == activeVisualizerIndex) {
            return
        }
        activeVisualizer?.stopLooping()
        if (visualizerSwitcher.displayedChild == 0 && viewIndex == 1) {
            visualizerSwitcher.showNext()
            if (PreferencesHelper.shouldShowPoseBackground(context)) {
                poseSequenceBackground.setImageURI(Uri.parse(PreferencesHelper.getPoseSequenceBackground(context)))
                poseSequenceBackground.visibility = View.VISIBLE
            } else {
                poseSequenceBackground.visibility = View.GONE
            }
        } else if (visualizerSwitcher.displayedChild == 1 && viewIndex == 0) {
            visualizerSwitcher.showPrevious()
            poseSequenceBackground.visibility = View.GONE
        }
        activeVisualizerIndex = visualizerIndex
        if (!activeVisualizer!!.isSourceLoaded) {
            activeVisualizer?.loadSource()
        }
        loopVisualizerForSelectedActivity()
    }

    private fun onVisualizerSourceLoaded() {
        visualizerPreparingIndicator.visibility = View.GONE
        Log.d("LabelsEditorDialog", "onVisualizerPrepared")
    }

    private fun onVisualizerStartLoadingSource() {
        visualizerPreparingIndicator.visibility = View.VISIBLE
        Log.d("LabelsEditorDialog", "onVisualizerPreparationStarted")
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
        activeVisualizer?.loopInterval(
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
                useCase.getActivityLabelsJSONFile(),
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
    private fun startOrConfirmSplit() {
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

    private class HorizontalSwipeDetector(val onHorizontalSwipe: (isSwipeToLeft: Boolean) -> Unit) :
        GestureDetector.SimpleOnGestureListener() {

        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            event1: MotionEvent,
            event2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.d("LabelsEditorDialog", "onFling: $event1 $event2")
            if (abs(velocityX) > abs(velocityY * 2)) {
                onHorizontalSwipe(velocityX < 0)
            }
            return true
        }
    }
}
