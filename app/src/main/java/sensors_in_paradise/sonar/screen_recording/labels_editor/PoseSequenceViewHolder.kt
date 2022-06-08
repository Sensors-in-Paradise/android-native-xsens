package sensors_in_paradise.sonar.screen_recording.labels_editor

import android.content.Context
import android.graphics.PointF
import android.view.TextureView
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.PoseEstimationStorageManager
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.VisualizationUtils
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.PoseSequence
import sensors_in_paradise.sonar.util.PreferencesHelper

class PoseSequenceViewHolder(
    private val context: Context,
    private val textureView: TextureView,
    onSourceLoadedListener: () -> Unit,
    onStartLoadingSource: () -> Unit,
    onSeekToNewPosition: ((ms: Long) -> Unit)? = null
) :
    VisualSequenceViewHolder(onSourceLoadedListener, onStartLoadingSource, onSeekToNewPosition) {
    private var poseSequence: PoseSequence? = null
    private var jointsToDraw: List<Pair<Int, Int>>? = null

    init {
        textureView.isOpaque = false
    }

    override fun loadSource(sourcePath: String, onSourceLoadedListener: () -> Unit) {
        poseSequence = PoseEstimationStorageManager.loadPoseSequenceFromCSV(context, sourcePath)
        jointsToDraw = VisualizationUtils.get2DLines(poseSequence!!.type)
        onSourceLoadedListener()
    }

    override fun seekTo(ms: Long) {
        super.seekTo(ms)
        poseSequence?.let { poseSequence ->
            val poses = getPosesAtTime(ms, poseSequence)
            drawOnCanvas(poses)
        }
    }

    private fun getPosesAtTime(ms: Long, poseSequence: PoseSequence): List<List<PointF>> {
        val timeStamp = poseSequence.startTime + ms
        val poseIndex = poseSequence.timeStamps.binarySearch(timeStamp)
        return if (poseIndex < -1) {
            // When timeStamp lies between two samples
            VisualizationUtils.interpolatePoses(
                poseSequence,
                -(poseIndex + 2),
                timeStamp
            )
        } else {
            poseSequence.posesArray.getOrElse(poseIndex) { listOf() }
                .filterNotNull().map { pose -> pose.map { PointF(it.x, it.y) } }
        }
    }

    private fun drawOnCanvas(poses: List<List<PointF>>) {
        textureView.lockCanvas()?.let { canvas ->
            VisualizationUtils.transformPoints(
                poses,
                textureView.bitmap,
                canvas,
                VisualizationUtils.Transformation.PROJECT_ON_CANVAS
            )
            VisualizationUtils.drawSkeleton(
                poses,
                canvas,
                jointsToDraw!!,
                clearColor = if (PreferencesHelper.shouldShowPoseBackground(context)) null
                    else context.getColor(R.color.slightBackgroundContrast),
                circleColor = context.getColor(R.color.stickmanJoints),
                lineColor = context.getColor(R.color.backgroundContrast)
            )
            textureView.unlockCanvasAndPost(canvas)
        }
    }
}
