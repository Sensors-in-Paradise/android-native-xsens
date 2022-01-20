package sensors_in_paradise.sonar.uploader

import java.io.File

class RecordingUIItemArrayList : ArrayList<RecordingUIItem>() {

    fun getRecordingsInDir(dir: File): ArrayList<RecordingUIItem> {
        val recordings = ArrayList<RecordingUIItem>()
        for (fileUIItem in this) {
            if (fileUIItem.dir.path.contains(dir.path)) {
                recordings.add(fileUIItem)
            }
        }
        return recordings
    }
    fun getByRecordingDir(dir: File): RecordingUIItem? {
        for (fileUIItem in this) {
            if (fileUIItem.dir == dir) {
                return fileUIItem
            }
        }
        return null
    }
    /*
    fun contains(file: File): Boolean {
        for (fileUIItem in this) {
            if (fileUIItem.file == file) {
                return true
            }
        }
        return false
    }
    fun indexOf(file: File): Int {
        for ((i, fileUIItem) in this.withIndex()) {
            if (fileUIItem.file == file) {
                return i
            }
        }
        return -1
    }
    fun setStatusOfAllItems(status: UploadStatus) {
        for (item in this) {
            item.status = status
        }
    }
    */
    fun areAllUploadedOrFailed(): Boolean {
        for (item in this) {
            if (item.isFailed() || item.isUploaded()) {
                return false
            }
        }
        return true
    }
    fun areAllUploaded(): Boolean {
        for (item in this) {
            if (item.isUploaded()) {
                return false
            }
        }
        return true
    }
}
