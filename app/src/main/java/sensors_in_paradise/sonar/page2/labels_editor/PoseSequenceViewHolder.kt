package sensors_in_paradise.sonar.page2.labels_editor

import android.graphics.Color
import android.view.TextureView
import sensors_in_paradise.sonar.page2.camera.pose_estimation.PoseEstimationStorageManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.VisualizationUtils
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.PoseSequence

class PoseSequenceViewHolder(
    private val textureView: TextureView,
    onSourceLoadedListener: () -> Unit,
    onStartLoadingSource: () -> Unit
) :
    VisualSequenceViewHolder(onSourceLoadedListener,onStartLoadingSource) {
    private var poseSequence: PoseSequence? = null

    init {
        textureView.isOpaque = false
    }

    override fun loadSource(sourcePath: String, onSourceLoadedListener: () -> Unit) {
        poseSequence = PoseEstimationStorageManager.loadPoseSequenceFromCSV(sourcePath)
        onSourceLoadedListener()
    }

    override fun seekTo(ms: Long) {
        try {
            poseSequence?.let { poseSequence ->
                val timeStamp = poseSequence.startTime + ms
                var poseIndex = poseSequence.timeStamps.binarySearch(timeStamp)
                var persons = listOf<Person>()
                if (poseIndex < -1) {
                    // timeStamp lies between two samples
                    persons = VisualizationUtils.interpolatePersons(
                        poseSequence,
                        -(poseIndex + 2),
                        timeStamp
                    )
                } else {
                    persons = poseSequence.personsArray.getOrElse(poseIndex) { listOf<Person>() }
                        .map { it.copy() }
                }

                textureView.lockCanvas()?.let { canvas ->
                    VisualizationUtils.transformKeypoints(
                        persons,
                        textureView.bitmap,
                        canvas,
                        VisualizationUtils.Transformation.PROJECT_ON_CANVAS
                    )
                    VisualizationUtils.drawBodyKeypoints(persons, canvas, Color.DKGRAY)
                    textureView.unlockCanvasAndPost(canvas)
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}
