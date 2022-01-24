package sensors_in_paradise.sonar.uploader

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.common.net.MediaType
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.MessageDialog
import sensors_in_paradise.sonar.page2.RecordingDataManager
import java.io.File

class OwnCloudRecordingsUploader(activity: Activity, val recordingsManager: RecordingDataManager) :
    OwnCloudClientInterface {
    val context: Context = activity
    var onItemChanged: ((recording: RecordingUIItem) -> Unit)? = null
    var onAllItemsFinishedWork: (() -> Unit)? = null
    private val ownCloudMetadata =
        LocalOwnCloudMetadataStorage(activity, GlobalValues.getSensorRecordingsBaseDir(context))
    private val ownCloud = OwnCloudClient(activity, this)
    val recordingUiItems = RecordingUIItemArrayList()
    private val dirCreationRequests = mutableSetOf<File>()
    init {
        reloadRecordings()
    }
    fun reloadRecordings() {
        recordingUiItems.clear()
        for (recording in recordingsManager.recordingsList) {
            val recording = RecordingUIItem(recording, ownCloudMetadata.localUploadedFilesBaseDir)

            for (file in recording.filesAndDirsToBeUploaded) {
                val isUploaded =
                    if (file.isFile) ownCloudMetadata.isFileUploaded(file) else ownCloudMetadata.isDirCreated(
                        file
                    )
                recording.setStatusOfFileOrDir(
                    file,
                    if (isUploaded) UploadStatus.UPLOADED else UploadStatus.NOT_UPLOADED
                )
            }
            if (recording.isUploaded()) {
                recordingUiItems.add(recording)
            } else {
                recordingUiItems.add(0, recording)
            }
        }
    }

    fun synchronize() {
        for (recordingUiItem in recordingUiItems) {
            if (recordingUiItem.areFilesValid) {
                uploadRecording(recordingUiItem)
            }
        }
        if (onAllItemsFinishedWork != null) {
            if (areAllRecordingsFinished()) {
                onAllItemsFinishedWork?.let { it() }
            }
        }
    }

    private fun uploadRecording(recording: RecordingUIItem) {
        if (ownCloudMetadata.isDirCreated(recording.dir)) {
            uploadFilesOfRecording(recording)
        } else {
            for (dir in recording.dirsToBeCreated) {
                val isCreated = ownCloudMetadata.isDirCreated(dir)
                val isCreationAlreadyRequested = dirCreationRequests.contains(dir)
                if (!isCreated && !isCreationAlreadyRequested) {
                    dirCreationRequests.add(dir)
                    recording.setStatusOfFileOrDir(dir, UploadStatus.UPLOADING)
                    ownCloud.createDir(ownCloudMetadata.getRelativePath(dir), dir)
                    break
                }
            }
        }
        onItemChanged?.let { it(recording) }
    }

    private fun uploadFilesOfRecording(recording: RecordingUIItem) {
        for (file in recording.filesToBeUploaded) {
            if (!ownCloudMetadata.isFileUploaded(file)) {
                Log.d("OWNCLOUD", "Uploading file: ${file.name}")
                recording.setStatusOfFileOrDir(file, UploadStatus.UPLOADING)
                ownCloud.uploadFile(
                    file,
                    ownCloudMetadata.getRelativePath(file),
                    MediaType.CSV_UTF_8
                )
            } else {
                // Log.d("OWNCLOUD", "File already uploaded: ${file.name}")
                recording.setStatusOfFileOrDir(file, UploadStatus.UPLOADED)
            }
            onItemChanged?.let { it(recording) }
        }
    }

    override fun onDirCreated(dirPath: String, localReferenceDir: File?) {
        Log.d("OWNCLOUD", "Dir created: $dirPath")
        ownCloudMetadata.setDirCreated(localReferenceDir!!)
        val recordings = recordingUiItems.getRecordingsInDir(localReferenceDir)
        for (recording in recordings) {
            recording.setStatusOfFileOrDir(localReferenceDir, UploadStatus.UPLOADED)
            uploadRecording(recording)

            onRecordingStatusChanged(recording)
        }
    }

    override fun onDirCreationFailed(dirPath: String, localReferenceDir: File?, e: Exception) {
        Log.e("OWNCLOUD", "Dir creation failed: ${e.message}")
        val recordings = recordingUiItems.getRecordingsInDir(localReferenceDir!!)
        for (recording in recordings) {
            if (recording.getStatusOfFileOrDir(localReferenceDir) != UploadStatus.UPLOADED) {
                recording.error = e
                recording.setStatusOfFileOrDir(localReferenceDir, UploadStatus.FAILED)
                onRecordingStatusChanged(recording)
            }
        }
    }

    override fun onFileUploaded(localFile: File, remoteFilePath: String) {
        Log.d("OWNCLOUD", "File Uploaded: $remoteFilePath")
        ownCloudMetadata.setFileUploaded(localFile)
        val recording = recordingUiItems.getByRecordingDir(localFile.parentFile!!)!!
        recording.setStatusOfFileOrDir(localFile, UploadStatus.UPLOADED)
        onRecordingStatusChanged(recording)
    }

    override fun onFileUploadFailed(localFile: File, filePath: String, e: Exception) {
        Log.e("OWNCLOUD", "Dir creation failed: ${e.message}")

        val recording = recordingUiItems.getByRecordingDir(localFile.parentFile!!)!!
        recording.error = e
        recording.setStatusOfFileOrDir(localFile, UploadStatus.FAILED)
        onRecordingStatusChanged(recording)
    }

    override fun onCredentialsNotAvailable() {
        MessageDialog(context, "Owncloud credentials are not available. " +
                "Please add apikeys.properties file to project root. (Ask Tobi)")
    }

    private fun onRecordingStatusChanged(recording: RecordingUIItem) {
        onItemChanged?.let { it(recording) }
        if (onAllItemsFinishedWork != null) {
            if (areAllRecordingsFinished()) {
                onAllItemsFinishedWork?.let { it() }
            }
        }
    }
    private fun areAllRecordingsFinished(): Boolean {
        for (recording in recordingUiItems) {
           if (!recording.isUploaded() && !recording.isFailed()) {
               return false
           }
        }
        return true
    }
}
