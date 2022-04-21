package sensors_in_paradise.sonar.page2.camera

import android.content.Context
import android.util.Log
import android.util.Size
import android.view.TextureView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.Recording

import androidx.camera.video.VideoCapture.withOutput
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import sensors_in_paradise.sonar.page2.LoggingManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.ModelType
import sensors_in_paradise.sonar.page2.camera.pose_estimation.MoveNet
import sensors_in_paradise.sonar.page2.camera.pose_estimation.ImageProcessor
import sensors_in_paradise.sonar.page2.camera.pose_estimation.PoseEstimationStorageManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Device
import sensors_in_paradise.sonar.util.PreferencesHelper
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.Executors

class CameraManager(
    val context: Context,
    private val previewView: PreviewView,
    overlayView: TextureView
) :
    Consumer<VideoRecordEvent> {
    private var cameraProvider: ProcessCameraProvider? = null
    private var isPreviewBound = false
    private val preview: Preview = Preview.Builder()
        .build()
    private val videoCaptureExecutor = Executors.newFixedThreadPool(2)
    private val recorder = Recorder.Builder()
        .setExecutor(videoCaptureExecutor)
        .setQualitySelector(QualitySelector.from(PreferencesHelper.getCameraRecordingQuality(context)))
        .build()
    private val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
    private val videoCapture: androidx.camera.video.VideoCapture<Recorder> = withOutput(recorder)
    private var videoRecording: Recording? = null

    private var imageProcessor: ImageProcessor? = null
    private var storageManager: PoseEstimationStorageManager? = null
    private val imageAnalysisExecutor = Executors.newFixedThreadPool(2)
    private val imageAnalysis = ImageAnalysis.Builder()
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setTargetResolution(Size(1280, 720)) //TODO generalize
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
            setAnalyzer(imageAnalysisExecutor, ImageAnalysis.Analyzer { imageProxy ->
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                // insert your code here.
                imageProxy.image?.let {
                    imageProcessor?.processImage(
                        it, overlayView
                    )
                }

                // after done, release the ImageProxy object
                imageProxy.close()
            })
        }

    init {
        ProcessCameraProvider.getInstance(context).apply {
            addListener({
                cameraProvider = this.get()
            }, ContextCompat.getMainExecutor(context))
        }
    }

    fun bindPreview(): Boolean {
        if (cameraProvider != null && !isPreviewBound) {
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val camera =
                cameraProvider?.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)
            isPreviewBound = camera != null
            Log.d("CameraManager", "Binding preview successful: $isPreviewBound")
        }
        return isPreviewBound
    }

    fun unbindPreview() {
        Log.d("CameraManager", "Unbinding preview")

        if (isPreviewBound) {
            isPreviewBound = false
            cameraProvider?.unbind(preview)
        }
    }

    private var isCaptureBound = false
    private fun bindVideoCapture(): Boolean {
        if (cameraProvider != null && !isCaptureBound) {
            val camera =
                cameraProvider?.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    videoCapture
                )
            isCaptureBound = camera != null
            Log.d("CameraManager", "Binding video capture successful: $isCaptureBound")
        }
        return isCaptureBound
    }

    private fun unbindVideoCapture() {
        Log.d("CameraManager", "Unbinding video capture...")
        isCaptureBound = false
        cameraProvider?.unbind(videoCapture)
    }

    private var videoFile: File? = null
    private var videoStartTime: Long = 0L

    fun startRecordingVideo(outputFile: File): Recording {
        if (!isCaptureBound) {
            bindVideoCapture()
        }
        videoFile = outputFile
        val outputOptions = FileOutputOptions.Builder(outputFile).build()

        val recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context), this)
        videoRecording = recording
        return recording
    }

    private var onRecordingFinalized: ((videoCaptureStartTime: Long, videoTempFile: File) -> Unit)? =
        null

    /** Stops the recording and returns the UNIX timestamp of
     * when the recording did actually start and the file where it's stored
     * */
    fun stopRecordingVideo(onRecordingFinalized: ((videoCaptureStartTime: Long, videoTempFile: File) -> Unit)? = null) {
        this.onRecordingFinalized = onRecordingFinalized
        if (videoRecording == null) {
            return
        }
        videoRecording?.close()
        unbindVideoCapture()
    }

    fun shouldRecordVideo(): Boolean {
        return PreferencesHelper.shouldStoreRawCameraRecordings(context)
    }


    override fun accept(t: VideoRecordEvent?) {
        when (t) {
            is VideoRecordEvent.Start -> {
                videoStartTime = LoggingManager.normalizeTimeStamp(LocalDateTime.now())
                Log.d("CameraManager", "Video Recording started")
            }
            is VideoRecordEvent.Finalize -> {
                onRecordingFinalized?.let { it(videoStartTime, videoFile!!) }
                Log.d("CameraManager", "Video Recording finalized")
            }
            is VideoRecordEvent.Pause -> {
                Log.d("CameraManager", "Video Recording paused")
            }
            is VideoRecordEvent.Status -> {
                Log.d("CameraManager", "Video Recording status updated")
            }
        }
    }

    private var isAnalyzerBound = false

    private fun bindImageAnalyzer(): Boolean {
        if (cameraProvider != null && !isAnalyzerBound) {
            val camera =
                cameraProvider?.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )
            isAnalyzerBound = camera != null
            Log.d("CameraManager", "Binding image analyzer successful: $isAnalyzerBound")
        }
        return isAnalyzerBound
    }

    private fun unbindImageAnalyzer() {
        Log.d("CameraManager", "Unbinding image analyzer...")
        isAnalyzerBound = false
        cameraProvider?.unbind(imageAnalysis)
    }

    private var poseStartTime: Long? = null

    fun startRecordingPose(outputFile: File) {
        if (!isAnalyzerBound) {
            bindImageAnalyzer()
        }
        val poseEstimator = createPoseEstimator()
        storageManager = storageManager ?: PoseEstimationStorageManager(outputFile)

        val localDateTime = LocalDateTime.now()
        // TODO use actual setting to get model type and dimensions
        storageManager!!.writeHeader(localDateTime, "LightningF16", 2)
        poseStartTime = LoggingManager.normalizeTimeStamp(localDateTime)

        imageProcessor = imageProcessor ?: ImageProcessor(poseEstimator, storageManager!!)
    }

    /** Stops the recording and returns the UNIX timestamp of
     * when the recording did actually start and the file where it's stored
     * MEIN CODE IST SCHEIßE - ALEX! */
    fun stopRecordingPose(onPoseRecordingFinalized: ((poseCaptureStartTime: Long, poseTempFile: File, poseEstimationStorageManager: PoseEstimationStorageManager) -> Unit)? = null) {
        if (imageProcessor != null && storageManager != null) {
            onPoseRecordingFinalized?.invoke(poseStartTime ?: 0L, storageManager!!.csvFile, storageManager!!)
        }
        unbindImageAnalyzer()
    }

    fun shouldRecordPose(): Boolean {
        return PreferencesHelper.shouldStorePoseEstimation(context)
    }

    private fun createPoseEstimator(): MoveNet {
        val modelType = ModelType.LightningF16
        val targetDevice = Device.CPU

        return MoveNet.create(context, targetDevice, modelType)
    }
}
