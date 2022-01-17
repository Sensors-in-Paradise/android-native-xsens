package sensors_in_paradise.sonar.uploader

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.common.net.MediaType
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.page2.Recording
import sensors_in_paradise.sonar.page2.RecordingDataManager
import java.io.File
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

class OwnCloudRecordingsUploader(activity: Activity, val recordingsManager: RecordingDataManager) :
    OwnCloudClientInterface {
    val context: Context = activity
    var onItemChanged: ((recording: RecordingUIItem) -> Unit)? = null
    private val ownCloudMetadata =
        LocalOwnCloudMetadataStorage(activity, GlobalValues.getSensorRecordingsBaseDir(context))
    private val ownCloud = OwnCloudClient(activity, this)
    val recordingUiItems = RecordingUIItemArrayList()

    init {
        for (recording in recordingsManager.recordingsList) {
            val recording = RecordingUIItem(recording)
            recordingUiItems.add(recording)
            recording.dirStatus =
                if (ownCloudMetadata.isDirCreated(recording.dir)) UploadStatus.UPLOADED else UploadStatus.NOT_UPLOADED
            for (file in recording.filesToBeUploaded) {
                recording.setStatusOfFile(
                    file,
                    if (ownCloudMetadata.isFileUploaded(file)) UploadStatus.UPLOADED else UploadStatus.NOT_UPLOADED
                )
            }
        }
    }

    fun synchronize() {
        for (recordingUiItem in recordingUiItems) {
            if (recordingUiItem.areFilesValid) {
                uploadRecording(recordingUiItem)
            }
        }
    }

    private fun uploadRecording(recording: RecordingUIItem) {
        val dir = recording.dir
        if (ownCloudMetadata.isDirCreated(dir)) {
            recording.dirStatus = UploadStatus.UPLOADED

            uploadFilesOfRecording(recording)
        } else {
            val path = ownCloudMetadata.getRelativePath(dir)
            recording.dirStatus = UploadStatus.UPLOADING
            ownCloud.createDir(path, dir)
        }
        onItemChanged?.let { it(recording) }
    }

    private fun uploadFilesOfRecording(recording: RecordingUIItem) {
        for (file in recording.filesToBeUploaded) {
            if (!ownCloudMetadata.isFileUploaded(file)) {
                Log.d("OWNCLOUD", "Uploading file: ${file.name}")
                recording.setStatusOfFile(file, UploadStatus.UPLOADING)

                ownCloud.uploadFile(
                    file,
                    ownCloudMetadata.getRelativePath(file),
                    MediaType.CSV_UTF_8
                )
            } else {
                Log.d("OWNCLOUD", "File already uploaded: ${file.name}")
                recording.setStatusOfFile(file, UploadStatus.UPLOADED)
            }
            onItemChanged?.let { it(recording) }
        }
    }

    override fun onDirCreated(dirPath: String, localReferenceDir: File?) {
        ownCloudMetadata.setDirCreated(localReferenceDir!!)
        val recording = recordingUiItems.getByDir(localReferenceDir)!!
        uploadFilesOfRecording(recording)
        recording.dirStatus = UploadStatus.UPLOADED
        onItemChanged?.let { it(recording) }
        Log.d("OWNCLOUD", "Dir created: $dirPath")

    }

    override fun onDirCreationFailed(dirPath: String, localReferenceDir: File?, e: Exception) {
        Log.e("OWNCLOUD", "Dir creation failed: ${e.message}")
        val recording = recordingUiItems.getByDir(localReferenceDir!!)!!
        recording.error = e
        recording.dirStatus = UploadStatus.FAILED
        onItemChanged?.let { it(recording) }
    }

    override fun onFileUploaded(localFile: File, remoteFilePath: String) {
        ownCloudMetadata.setFileUploaded(localFile)
        Log.d("OWNCLOUD", "File Uploaded: $remoteFilePath")
        val recording = recordingUiItems.getByDir(localFile.parentFile!!)!!
        recording.setStatusOfFile(localFile, UploadStatus.UPLOADED)
        onItemChanged?.let { it(recording) }
    }

    override fun onFileUploadFailed(localFile: File, filePath: String, e: Exception) {
        e.printStackTrace()
        Log.e("OWNCLOUD", "Dir creation failed: ${e.message}")
        val recording = recordingUiItems.getByDir(localFile.parentFile!!)!!
        recording.error = e
        recording.setStatusOfFile(localFile, UploadStatus.FAILED)
        onItemChanged?.let { it(recording) }
    }
}