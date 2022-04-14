// TODO: Has to be merged with existing camera implementation

package sensors_in_paradise.sonar.page2.camera.pose_estimation


import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.media.Image
import android.util.Log
import android.view.TextureView
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person

class SkeletonDrawer(private val poseDetector: PoseDetector)
{
    companion object {
        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .2f
        private const val TAG = "Camera Source"
    }

    private val lock = Any()
    private val isTrackerEnabled = false

    private fun imageToBitmap(image: Image): Bitmap {
        val buffer = image.planes[0].buffer

        val pixelStride: Int = image.planes[0].pixelStride
        val rowStride: Int = image.planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap //.rotate(90f)
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
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
        //Log.d("CameraManager", "num persons: ${persons.size}, num first person: ${if (persons.size > 0) persons[0].keyPoints.size else -1}")
        visualize(persons, bitmap, overlayView)
    }

    private fun visualize(persons: List<Person>, bitmap: Bitmap, overlayView: TextureView) {

       /*  val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            bitmap,
            persons.filter { it.score > MIN_CONFIDENCE }, isTrackerEnabled
        ) */

        val surfaceCanvas = overlayView.lockCanvas()
        surfaceCanvas?.let { canvas ->
            //canvas.rotate(90f, canvas.width.toFloat() / 2f, canvas.height.toFloat() / 2f)
            VisualizationUtils.drawBodyKeypoints(
                bitmap,
                canvas,
                persons.filter { it.score > MIN_CONFIDENCE }, isTrackerEnabled
            )
            /*val screenWidth: Int
            val screenHeight: Int
            val left: Int
            val top: Int

            /* if (canvas.height > canvas.width) {
                val ratio = outputBitmap.height.toFloat() / outputBitmap.width
                screenWidth = canvas.width
                left = 0
                screenHeight = (canvas.width * ratio).toInt()
                top = (canvas.height - screenHeight) / 2
            } else { */
                 val ratio = outputBitmap.width.toFloat() / outputBitmap.height
                 screenHeight = canvas.height
                 top = 0
                 screenWidth = (canvas.height * ratio).toInt()
                 left = (canvas.width - screenWidth) / 2
            //}
            val right: Int = left + screenWidth
            val bottom: Int = top + screenHeight

            canvas.rotate(90f, screenHeight.toFloat() / 2f, screenWidth.toFloat() / 2f)
            canvas.drawBitmap(
                outputBitmap, Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, right, bottom), null
            ) */

            overlayView.unlockCanvasAndPost(canvas)
        }
    }
}
