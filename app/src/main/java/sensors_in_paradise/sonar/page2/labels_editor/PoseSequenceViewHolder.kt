package sensors_in_paradise.sonar.page2.labels_editor

import android.graphics.Color
import android.view.TextureView
import sensors_in_paradise.sonar.page2.camera.pose_estimation.PoseEstimationStorageManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.VisualizationUtils
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.PoseSequence

class PoseSequenceViewHolder(
    private val textureView: TextureView,
    onPreparedListener: () -> Unit
) :
    VisualSequenceViewHolder(onPreparedListener) {
    private var poseSequence: PoseSequence? = null
    init {
        textureView.isOpaque = false
    }

    override fun loadSource(sourcePath: String) {
        poseSequence = PoseEstimationStorageManager.loadPoseSequenceFromCSV(sourcePath)
        onPreparedListener()
    }

    override fun seekTo(ms: Long) {
        try {
            poseSequence?.let { poseSequence ->
                val timeStamp = poseSequence.startTime + ms
                var poseIndex = poseSequence.timeStamps.binarySearch(timeStamp)
                poseIndex = if (poseIndex < -1) -(poseIndex + 1) else poseIndex
                val persons = poseSequence.personsArray.getOrElse(poseIndex) { listOf<Person>() }
                    .map { it.copy() }

                textureView.lockCanvas()?.let { canvas ->
                    VisualizationUtils.transformKeypoints(
                        persons,
                        textureView.bitmap,
                        canvas,
                        VisualizationUtils.Transformation.PROJECT_ON_CANVAS
                    )
                    VisualizationUtils.drawBodyKeypoints(canvas, persons, Color.DKGRAY)
                    textureView.unlockCanvasAndPost(canvas)
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}
