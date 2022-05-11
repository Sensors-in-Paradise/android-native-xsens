package sensors_in_paradise.sonar.uploader

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class LocalDavCloudMetadataStorage(context: Context, val localUploadedFilesBaseDir: File) :
    JSONStorage(File(context.dataDir, "davCloudMetadata.json")) {
    private lateinit var uploadedFilesAndDirsObj: JSONObject
    override fun onFileNewlyCreated() {
        uploadedFilesAndDirsObj = JSONObject()
        json.put("uploadedFilesAndDirs", uploadedFilesAndDirsObj)
    }

    override fun onJSONInitialized() {
        uploadedFilesAndDirsObj = json.getJSONObject("uploadedFilesAndDirs")
    }

    fun setFileUploaded(file: File) {
        setFileUploaded(getRelativePath(file.parentFile ?: localUploadedFilesBaseDir), file.name)
    }

	fun isFileUploaded(file: File): Boolean {
        return isFileUploaded(getRelativePath(file.parentFile ?: localUploadedFilesBaseDir), file.name)
    }

    fun setDirCreated(dir: File) {
        setDirCreated(getRelativePath(dir))
    }

    fun isDirCreated(dir: File): Boolean {
        return isDirUploaded(getRelativePath(dir))
    }

    fun getRelativePath(dirOrFile: File): String {
        return dirOrFile.absolutePath.removePrefix(localUploadedFilesBaseDir.absolutePath)
    }

    private fun setDirCreated(dirPath: String) {
        val path = DavCloudClient.normalizePath(dirPath).removeSuffix("/")
        val dirs = path.split("/")
        var dirObj = uploadedFilesAndDirsObj
        for (dir in dirs) {
            var childObj = dirObj.optJSONObject(dir)
            if (childObj == null) {
                childObj = JSONObject()
            }
            dirObj.put(dir, childObj)
            dirObj = childObj
        }
        save()
    }

    private fun setFileUploaded(dirPath: String, fileName: String) {
        val path = DavCloudClient.normalizePath(dirPath).removeSuffix("/")
        val dirs = path.split("/")
        var dirObj = uploadedFilesAndDirsObj
        for (dir in dirs) {
            if (dir != "") {
                var childObj = dirObj.optJSONObject(dir)
                if (childObj == null) {
                    childObj = JSONObject()
                }
                dirObj.put(dir, childObj)
                dirObj = childObj
            }
        }
        var array = dirObj.optJSONArray("files")
        if (array == null) {
            array = JSONArray()
            dirObj.put("files", array)
        }
        if (!doesJSONArrayContain(array, fileName)) {
            array.put(fileName)
            save()
        }
    }

    private fun isFileUploaded(dirPath: String, fileName: String): Boolean {
        val path = DavCloudClient.normalizePath(dirPath).removeSuffix("/")
        val dirs = path.split("/")
        var dirObj = uploadedFilesAndDirsObj

        for (dir in dirs) {
            if (dir != "") {
                val childObj = dirObj.optJSONObject(dir)
                if (childObj != null) {
                    dirObj = childObj
                } else {
                    return false
                }
            }
        }
        val array = dirObj.optJSONArray("files")
        return doesJSONArrayContain(array, fileName)
    }

    private fun isDirUploaded(dirPath: String): Boolean {
        val path = DavCloudClient.normalizePath(dirPath).removeSuffix("/")
        val dirs = path.split("/")
        var dirObj = uploadedFilesAndDirsObj

        for (dir in dirs) {
            val childObj = dirObj.optJSONObject(dir)
            if (childObj != null) {
                dirObj = childObj
            } else {
                return false
            }
        }
        return true
    }

    private fun doesJSONArrayContain(array: JSONArray?, item: String): Boolean {
        if (array != null) {
            for (i in 0 until array.length()) {
                if (array[i] == item) {
                    return true
                }
            }
        }
        return false
    }
}
