package sensors_in_paradise.sonar.page2.camera.pose_estimation

import android.content.Context
import android.graphics.*
import android.media.Image
import android.view.TextureView
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person

class ImageProcessor(
    private val context: Context,
    private val poseDetector: PoseDetector,
    private val poseEstimationStorageManager: PoseEstimationStorageManager
) {
    companion object {
        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .4f
        const val POSE_ESTIMATION_FREQUENCY = 25
    }

    private val lock = Any()

    private fun imageToBitmap(image: Image): Bitmap {
        val buffer = image.planes[0].buffer

        val pixelStride: Int = image.planes[0].pixelStride
        val rowStride: Int = image.planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun extractPoses(bitmap: Bitmap): List<Person> {
        val persons = mutableListOf<Person>()
        synchronized(lock) {
            poseDetector.estimatePoses(bitmap).let {
                persons.addAll(it)
            }
        }
        return persons.filter { it.score > MIN_CONFIDENCE }.toList()
    }

    private fun drawOnCanvas(persons: List<Person>, overlayView: TextureView, bitmap: Bitmap, isRotated90: Boolean) {
        val surfaceCanvas = overlayView.lockCanvas()
        surfaceCanvas?.let { canvas ->
            VisualizationUtils.transformKeyPoints(
                persons, bitmap, canvas,
                VisualizationUtils.Transformation.PROJECT_ON_CANVAS,
                isRotated90
            )

            VisualizationUtils.drawBodyKeyPoints(
                persons,
                canvas,
                circleColor = context.getColor(R.color.stickmanJoints),
                lineColor = context.getColor(R.color.slightBackgroundContrast)
            )

            overlayView.unlockCanvasAndPost(canvas)
        }
    }

    fun clearView(overlayView: TextureView) {
        val surfaceCanvas = overlayView.lockCanvas()
        surfaceCanvas?.let { canvas ->
            VisualizationUtils.drawBodyKeyPoints(listOf<Person>(), canvas)
            overlayView.unlockCanvasAndPost(canvas)
        }
    }

    fun processImage(bitmap: Bitmap, overlayView: TextureView, isRotated90: Boolean) {
        val persons = extractPoses(bitmap)

        VisualizationUtils.transformKeyPoints(
            persons, bitmap, null,
            VisualizationUtils.Transformation.NORMALIZE
        )
        if (isRotated90) {
            VisualizationUtils.transformKeyPoints(
                persons, null, null,
                VisualizationUtils.Transformation.ROTATE90
            )
        }
        poseEstimationStorageManager.storePoses(persons)

        drawOnCanvas(persons, overlayView, bitmap, isRotated90)
    }

    fun processImage(image: Image, overlayView: TextureView, isRotated90: Boolean) {
        processImage(imageToBitmap(image), overlayView, isRotated90)
    }
}
