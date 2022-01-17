package sensors_in_paradise.sonar.uploader

import android.app.Activity
import android.content.Context
import com.google.common.net.MediaType
import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import sensors_in_paradise.sonar.BuildConfig
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class OwnCloudClient(val activity: Activity, val callback: OwnCloudClientInterface) {
    val context: Context = activity
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val sardine: Sardine = OkHttpSardine()
    private val baseUrl = BuildConfig.OWNCLOUD_BASEURL
    init{
        sardine.setCredentials(BuildConfig.OWNCLOUD_USER, BuildConfig.OWNCLOUD_TOKEN)
    }
    fun createDir(remoteDirPath:String, localReferenceDir: File? = null){
        executor.execute{
            try {
                sardine.createDirectory("${baseUrl}${normalizePath(remoteDirPath)}")
                activity.runOnUiThread{callback.onDirCreated(remoteDirPath, localReferenceDir)}
            }
            catch(e: Exception){
                activity.runOnUiThread{callback.onDirCreationFailed(remoteDirPath,localReferenceDir, e)}
            }
        }
    }
    fun uploadFile(localFile: File, remoteFilePath:String, mediaType: MediaType){
        executor.execute{
            try {
                sardine.put("${baseUrl}${normalizePath(remoteFilePath)}", localFile, mediaType.type())
                activity.runOnUiThread{callback.onFileUploaded(localFile, remoteFilePath)}
            }
            catch(e: Exception){
                activity.runOnUiThread{ callback.onFileUploadFailed(localFile,remoteFilePath, e)}
            }
        }
    }
    companion object {
        fun normalizePath(path: String): String {
            if (path.startsWith("/")) {
                return path.replaceFirst("/", "")
            }
            if (path.startsWith("./")) {
                return path.replaceFirst("./", "")
            }
            return path
        }
    }
}