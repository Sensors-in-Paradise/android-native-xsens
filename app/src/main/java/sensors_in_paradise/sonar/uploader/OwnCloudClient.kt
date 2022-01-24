package sensors_in_paradise.sonar.uploader

import android.app.Activity
import android.content.Context
import com.google.common.net.MediaType
import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import sensors_in_paradise.sonar.BuildConfig
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OwnCloudClient(val activity: Activity, val callback: OwnCloudClientInterface) {
    val context: Context = activity
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val sardine: Sardine = OkHttpSardine()
    private val baseUrl = BuildConfig.OWNCLOUD_BASEURL
    private val credentialsAvailable = BuildConfig.OWNCLOUD_CREDENTIALS_AVAILABLE
    private val credentialsNotAvailableException =
        Exception("Owncloud credentials are not available. " +
                "Please add apikeys.properties file to project root. (Ask Tobi)")

    init {
        if (credentialsAvailable) {
            sardine.setCredentials(BuildConfig.OWNCLOUD_USER, BuildConfig.OWNCLOUD_TOKEN)
        } else {
            callback.onCredentialsNotAvailable()
        }
    }

    fun createDir(remoteDirPath: String, localReferenceDir: File? = null) {
        if (credentialsAvailable) {
            executor.execute {
                try {
                    sardine.createDirectory("${baseUrl}${normalizePath(remoteDirPath)}")
                    activity.runOnUiThread {
                        callback.onDirCreated(
                            remoteDirPath,
                            localReferenceDir
                        )
                    }
                } catch (e: IOException) {
                    activity.runOnUiThread {
                        callback.onDirCreationFailed(
                            remoteDirPath,
                            localReferenceDir,
                            e
                        )
                    }
                }
            }
        } else {
            callback.onDirCreationFailed(
                remoteDirPath,
                localReferenceDir,
                credentialsNotAvailableException
            )
        }
    }

    fun uploadFile(localFile: File, remoteFilePath: String, mediaType: MediaType) {
        if (credentialsAvailable) {
            executor.execute {
                try {
                    sardine.put(
                        "${baseUrl}${normalizePath(remoteFilePath)}",
                        localFile,
                        mediaType.type()
                    )
                    activity.runOnUiThread { callback.onFileUploaded(localFile, remoteFilePath) }
                } catch (e: IOException) {
                    activity.runOnUiThread {
                        callback.onFileUploadFailed(
                            localFile,
                            remoteFilePath,
                            e
                        )
                    }
                }
            }
        } else {
            callback.onFileUploadFailed(localFile, remoteFilePath, credentialsNotAvailableException)
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
