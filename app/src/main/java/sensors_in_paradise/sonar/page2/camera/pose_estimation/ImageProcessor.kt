package sensors_in_paradise.sonar.page2.camera.pose_estimation


import android.graphics.*
import android.media.Image
import android.view.TextureView
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person

class ImageProcessor(private val poseDetector: PoseDetector, private val storageManager: StorageManager) {
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

    // process image
    fun processImage(image: Image, overlayView: TextureView) {
        val bitmap = imageToBitmap(image)
        val persons = mutableListOf<Person>()

        synchronized(lock) {
            poseDetector.estimatePoses(bitmap).let {
                persons.addAll(it)
            }
        }

        VisualizationUtils.transformKeypoints(
            persons, bitmap, null,
            VisualizationUtils.Transformation.NORMALIZE
        )
        VisualizationUtils.transformKeypoints(
            persons, null, null,
            VisualizationUtils.Transformation.ROTATE90
        )

        storageManager.storePoses(persons)

        val surfaceCanvas = overlayView.lockCanvas()
        surfaceCanvas?.let { canvas ->
            VisualizationUtils.transformKeypoints(
                persons, bitmap, canvas,
                VisualizationUtils.Transformation.PROJECT_ON_CANVAS
            )

            VisualizationUtils.drawBodyKeypoints(
                canvas,
                persons.filter { it.score > MIN_CONFIDENCE }
            )
            overlayView.unlockCanvasAndPost(canvas)
        }
    }

}
