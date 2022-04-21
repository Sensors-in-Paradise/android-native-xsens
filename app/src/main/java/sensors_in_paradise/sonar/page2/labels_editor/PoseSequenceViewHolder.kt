package sensors_in_paradise.sonar.page2.labels_editor

import android.view.TextureView
import sensors_in_paradise.sonar.page2.camera.pose_estimation.PoseEstimationStorageManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.VisualizationUtils
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.PoseSequence

class PoseSequenceViewHolder(
    private val storageManager: PoseEstimationStorageManager,
    val textureView: TextureView,
    onPreparedListener: () -> Unit
) :
    VisualSequenceViewHolder(onPreparedListener) {
    private var poseSequence: PoseSequence? = null

    override fun loadSource(sourcePath: String) {
        poseSequence = storageManager.loadPoseSequenceFromCSV(sourcePath)
        onPreparedListener()
    }

    override fun seekTo(ms: Long) {
        try {
            if (poseSequence != null) {
                val poseIndex =
                    poseSequence!!.timeStamps.binarySearch(poseSequence!!.startTime + ms)
                val persons = poseSequence!!.personsArray[poseIndex]
                textureView.lockCanvas()?.let { canvas ->
                    VisualizationUtils.transformKeypoints(
                        persons,
                        textureView.bitmap,
                        canvas,
                        VisualizationUtils.Transformation.PROJECT_ON_CANVAS
                    )
                    VisualizationUtils.drawBodyKeypoints(canvas, persons)
                }
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val FPS = 60L
    }
}
