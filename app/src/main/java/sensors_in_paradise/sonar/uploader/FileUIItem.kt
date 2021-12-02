package sensors_in_paradise.sonar.uploader

import java.io.File
import java.lang.Exception

class FileUIItem(val file: File, val label: String) {
    var status = UploadStatus.NOT_UPLOADED
    var error: Exception? = null

    fun statusLabel(): String {
        val errorString = if (error != null) error!!.message else ""
        return when (status) {
            UploadStatus.NOT_UPLOADED -> "Not uploaded yet"
            UploadStatus.UPLOADED -> "Uploaded"
            UploadStatus.FAILED -> "Upload failed: $errorString"
            UploadStatus.UPLOADING -> "Uploading"
        }
    }
}
