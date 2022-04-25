package sensors_in_paradise.sonar.page2.camera.pose_estimation


import android.graphics.*
import android.media.Image
import android.view.TextureView
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person

class ImageProcessor(
    private val poseDetector: PoseDetector,
    private val poseEstimationStorageManager: PoseEstimationStorageManager
) {
    companion object {
        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .4f
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

    fun clearView(overlayView: TextureView) {
        val surfaceCanvas = overlayView.lockCanvas()
        surfaceCanvas?.let { canvas ->
            VisualizationUtils.drawBodyKeypoints(listOf<Person>(), canvas)
            overlayView.unlockCanvasAndPost(canvas)
        }
    }

    // process image
    fun processImage(bitmap: Bitmap, overlayView: TextureView, isRotated90: Boolean) {
        var persons = mutableListOf<Person>()
        synchronized(lock) {
            poseDetector.estimatePoses(bitmap).let {
                persons.addAll(it)
            }
        }
        persons = persons.filter { it.score > MIN_CONFIDENCE }.toMutableList()

        VisualizationUtils.transformKeypoints(
            persons, bitmap, null,
            VisualizationUtils.Transformation.NORMALIZE
        )
        if (isRotated90) {
            VisualizationUtils.transformKeypoints(
                persons, null, null,
                VisualizationUtils.Transformation.ROTATE90
            )
        }

        poseEstimationStorageManager.storePoses(persons)

        val surfaceCanvas = overlayView.lockCanvas()
        surfaceCanvas?.let { canvas ->
            VisualizationUtils.transformKeypoints(
                persons, bitmap, canvas,
                VisualizationUtils.Transformation.PROJECT_ON_CANVAS
            )

            VisualizationUtils.drawBodyKeypoints(
                persons,
                canvas
            )

            overlayView.unlockCanvasAndPost(canvas)
        }
    }

    fun processImage(image: Image, overlayView: TextureView, isRotated90: Boolean) {
        processImage(imageToBitmap(image), overlayView, isRotated90)
    }

}
