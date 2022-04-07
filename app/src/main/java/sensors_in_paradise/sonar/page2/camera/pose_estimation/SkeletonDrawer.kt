// TODO: Has to be merged with existing camera implementation

package sensors_in_paradise.sonar.page2.camera.pose_estimation


import android.graphics.Bitmap
import android.graphics.Rect
import android.media.Image
import android.view.SurfaceView
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person
class SkeletonDrawer
// private val listener: CameraSourceListener? = null
{
    companion object {
        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .2f
        private const val TAG = "Camera Source"
    }

    private val lock = Any()
    private var detector: PoseDetector? = null
    private val isTrackerEnabled = false
    private lateinit var imageBytes: ByteArray


    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    private fun imageToBitmap(image: Image): Bitmap {
        val buffer = image.planes[0].buffer;

        val pixelStride: Int = image.planes[0].pixelStride
        val rowStride: Int = image.planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap
    }

    // process image
    fun processImage(image: Image, overlayView: SurfaceView) {
        val bitmap = imageToBitmap(image)
        val persons = mutableListOf<Person>()

        synchronized(lock) {
            detector?.estimatePoses(bitmap)?.let {
                persons.addAll(it)
            }
        }
        // frameProcessedInOneSecondInterval++
        // if (frameProcessedInOneSecondInterval == 1) {
        //    // send fps to view
        //    listener?.onFPSListener(framesPerSecond)
        // }

        visualize(persons, bitmap, overlayView)
    }

    private fun visualize(persons: List<Person>, bitmap: Bitmap, overlayView: SurfaceView) {

        val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            bitmap,
            persons.filter { it.score > MIN_CONFIDENCE }, isTrackerEnabled
        )

        val holder = overlayView.holder
        val surfaceCanvas = holder.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val screenHeight: Int
            val left: Int
            val top: Int

            if (canvas.height > canvas.width) {
                val ratio = outputBitmap.height.toFloat() / outputBitmap.width
                screenWidth = canvas.width
                left = 0
                screenHeight = (canvas.width * ratio).toInt()
                top = (canvas.height - screenHeight) / 2
            } else {
                val ratio = outputBitmap.width.toFloat() / outputBitmap.height
                screenHeight = canvas.height
                top = 0
                screenWidth = (canvas.height * ratio).toInt()
                left = (canvas.width - screenWidth) / 2
            }
            val right: Int = left + screenWidth
            val bottom: Int = top + screenHeight

            canvas.drawBitmap(
                outputBitmap, Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, right, bottom), null
            )
            overlayView.holder.unlockCanvasAndPost(canvas)
        }
    }

    fun setDetector(detector: PoseDetector) {
        synchronized(lock) {
            if (this.detector != null) {
                this.detector?.close()
                this.detector = null
            }
            this.detector = detector
        }
    }
}
