package sensors_in_paradise.sonar.page2.labels_editor

import android.content.Context
import android.view.TextureView
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page2.camera.pose_estimation.PoseEstimationStorageManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.VisualizationUtils
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.PoseSequence

class PoseSequenceViewHolder(
    private val context: Context,
    private val textureView: TextureView,
    onSourceLoadedListener: () -> Unit,
    onStartLoadingSource: () -> Unit
) :
    VisualSequenceViewHolder(onSourceLoadedListener, onStartLoadingSource) {
    private var poseSequence: PoseSequence? = null

    init {
        textureView.isOpaque = false
    }

    override fun loadSource(sourcePath: String, onSourceLoadedListener: () -> Unit) {
        poseSequence = PoseEstimationStorageManager.loadPoseSequenceFromCSV(context, sourcePath)
        onSourceLoadedListener()
    }

    override fun seekTo(ms: Long) {
        poseSequence?.let { poseSequence ->
            val persons = getPosesAtTime(ms, poseSequence)
            drawOnCanvas(persons)
        }
    }

    private fun getPosesAtTime(ms: Long, poseSequence: PoseSequence): List<Person> {
        val timeStamp = poseSequence.startTime + ms
        val poseIndex = poseSequence.timeStamps.binarySearch(timeStamp)
        return if (poseIndex < -1) {
            // When timeStamp lies between two samples
            VisualizationUtils.interpolatePersons(
                poseSequence,
                -(poseIndex + 2),
                timeStamp
            )
        } else {
            poseSequence.personsArray.getOrElse(poseIndex) { listOf<Person>() }
                .map { it.copy() }
        }
    }

    private fun drawOnCanvas(persons: List<Person>) {
        textureView.lockCanvas()?.let { canvas ->
            VisualizationUtils.transformKeyPoints(
                persons,
                textureView.bitmap,
                canvas,
                VisualizationUtils.Transformation.PROJECT_ON_CANVAS,
                false
            )
            VisualizationUtils.drawBodyKeyPoints(
                persons,
                canvas,
                clearColor = context.getColor(R.color.slightBackgroundContrast),
                circleColor = context.getColor(R.color.stickmanJoints),
                lineColor = context.getColor(R.color.backgroundContrast)
            )
            textureView.unlockCanvasAndPost(canvas)
        }
    }
}
