package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import android.content.Context
import android.graphics.*
import android.media.Image
import android.view.TextureView
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.BodyPart
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.KeyPoint
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.Person

class ImageProcessor(
    private val context: Context,
    val poseDetector: PoseDetector,
    private val poseEstimationStorageManager: PoseEstimationStorageManager
) {
    companion object {
        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .4f
        const val POSE_ESTIMATION_FREQUENCY = 25

        const val INPUT_WIDTH = 1280
        const val INPUT_HEIGHT = 720
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

    fun getDummyPose(): Person {
        val kpList = mutableListOf<KeyPoint>()
        kpList.add(KeyPoint(BodyPart.NOSE, PointF(0.3f, 0.6f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_EYE, PointF(0.3f, 324.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_EYE, PointF(0.3f, 54.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_EAR, PointF(30.3f, 324.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_EAR, PointF(32.3f, 45.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_SHOULDER, PointF(50.3f, 345.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_SHOULDER, PointF(765.3f, 213.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_ELBOW, PointF(67.3f, 345.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_ELBOW, PointF(345.3f, 2.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_WRIST, PointF(264.3f, 89.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_WRIST, PointF(23.3f, 678.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_HIP, PointF(263.3f, 43.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_HIP, PointF(423.3f, 3.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_KNEE, PointF(846.3f, 1257.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_KNEE, PointF(676.3f, 65.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.LEFT_ANKLE, PointF(87.3f, 345.7f), 0.93f))
        kpList.add(KeyPoint(BodyPart.RIGHT_ANKLE, PointF(56.3f, 877.7f), 0.93f))

        return Person(3, kpList, null, 0.92f)
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
