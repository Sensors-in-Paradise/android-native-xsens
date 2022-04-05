package sensors_in_paradise.sonar.page2

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraManager(val context: Context, private val previewView: PreviewView) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var isPreviewBound = false
    init {
        ProcessCameraProvider.getInstance(context).apply {
            addListener({
                cameraProvider = this.get()
                isPreviewBound = bindPreview()
            }, ContextCompat.getMainExecutor(context))
        }
    }
    private val preview: Preview = Preview.Builder()
        .build()

    fun bindPreview(): Boolean {
        if (cameraProvider != null && !isPreviewBound) {
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val camera =
                cameraProvider?.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview)
            return camera != null
        }
        return isPreviewBound
    }

    fun unbindPreview() {
        isPreviewBound = false
        cameraProvider?.unbind(preview)
    }
}
