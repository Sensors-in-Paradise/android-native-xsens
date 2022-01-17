package sensors_in_paradise.sonar.uploader

import sensors_in_paradise.sonar.page2.Recording
import java.io.File
import java.lang.Exception

class RecordingUIItem(recording: Recording): Recording(recording) {
    var dirStatus = UploadStatus.NOT_UPLOADED
    var error: Exception? = null
    val filesToBeUploaded = getFilesToBeUploadedList()
    private val fileUploadStatus: MutableMap<File, UploadStatus> =
        recording.getRecordingFiles().associate { it to UploadStatus.NOT_UPLOADED } as MutableMap<File, UploadStatus>
       fun getStatusOfDir(): UploadStatus{
        return dirStatus
    }
    private fun getFilesToBeUploadedList(): ArrayList<File>{
        val files = ArrayList<File>( )
        files.addAll(getRecordingFiles())
        files.add(metadataStorage.file)
        return files
    }
    fun getSummarizedStatus(): UploadStatus{
        if(dirStatus == UploadStatus.UPLOADING){
            return UploadStatus.UPLOADING
        }
        if(dirStatus == UploadStatus.FAILED){
            return UploadStatus.FAILED
        }
        if(dirStatus == UploadStatus.NOT_UPLOADED){
            return UploadStatus.NOT_UPLOADED
        }
        for((file, status) in fileUploadStatus){
            if(status == UploadStatus.UPLOADING){
                return UploadStatus.UPLOADING
            }
        }
        var areAllFailed = true
        for((file, status) in fileUploadStatus){
            if(status!=UploadStatus.FAILED){
                areAllFailed = false
            }
        }
        if(areAllFailed){
            return UploadStatus.FAILED
        }
        return UploadStatus.UNKNOWN
    }
    fun getEmojiStatusOfFile(file: File): String{
       return statusEmoji(getStatusOfFile(file))
    }
    fun setStatusOfFile(file: File, status: UploadStatus){
        fileUploadStatus[file] = status
    }
    fun getStatusOfFile(file: File): UploadStatus{
        if(dirStatus != UploadStatus.UPLOADED){
            return UploadStatus.WAITING_FOR_PARENT
        }
        if(fileUploadStatus.containsKey(file)) {
            return fileUploadStatus[file]!!
        }
        return UploadStatus.UNKNOWN
    }
    fun getStatusLabelOfDir():String{
        return "Directory Status: ${statusLabel(dirStatus)}"
    }
    fun getStatusLabelOfFile(file: File):String{
        return statusLabel(getStatusOfFile(file))
    }
    fun isUploaded(): Boolean{
        if(dirStatus == UploadStatus.UPLOADED){
            for(file in getRecordingFiles()){
                if(getStatusOfFile(file)!=UploadStatus.UPLOADED){
                    return false
                }
            }
            return true
        }
        return false
    }
    fun isFailed(): Boolean{
        if(dirStatus == UploadStatus.FAILED){
           return true
        }
        for(file in getRecordingFiles()){
            if(getStatusOfFile(file)==UploadStatus.FAILED){
                return true
            }
        }
        return false
    }
    private fun statusEmoji(status: UploadStatus): String {
        return when (status) {
            UploadStatus.NOT_UPLOADED -> "\uD83D\uDCC5"
            UploadStatus.UPLOADED -> "✔️"
            UploadStatus.FAILED -> "❌"
            UploadStatus.UPLOADING -> "⏫"
            UploadStatus.UNKNOWN -> "❓"
            UploadStatus.WAITING_FOR_PARENT -> "⏳"
        }
    }
    private fun statusLabel(status: UploadStatus): String {
        val errorString = if (error != null) error!!.message else ""
        return "${statusEmoji(status)} "+when (status) {
            UploadStatus.NOT_UPLOADED -> "Not uploaded yet"
            UploadStatus.UPLOADED -> "Uploaded"
            UploadStatus.FAILED -> "Upload failed: $errorString"
            UploadStatus.UPLOADING -> "Uploading"
            UploadStatus.UNKNOWN -> "Unknown"
            UploadStatus.WAITING_FOR_PARENT -> "Waiting for parent dir to be uploaded"
        }
    }
}
