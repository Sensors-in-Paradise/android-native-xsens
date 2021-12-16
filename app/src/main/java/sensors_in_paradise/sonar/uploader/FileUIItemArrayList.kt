package sensors_in_paradise.sonar.uploader

import java.io.File

class FileUIItemArrayList : ArrayList<FileUIItem>() {
    fun get(file: File): FileUIItem? {
        for (fileUIItem in this) {
            if (fileUIItem.file == file) {
                return fileUIItem
            }
        }
        return null
    }
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
    fun areAllUploadedOrFailed(): Boolean {
        for (item in this) {
            if (item.status != UploadStatus.UPLOADED && item.status != UploadStatus.FAILED) {
                return false
            }
        }
        return true
    }
    fun areAllUploaded(): Boolean {
        for (item in this) {
            if (item.status != UploadStatus.UPLOADED) {
                return false
            }
        }
        return true
    }
}
