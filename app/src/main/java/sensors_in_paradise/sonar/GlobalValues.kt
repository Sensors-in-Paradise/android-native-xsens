package sensors_in_paradise.sonar

import android.Manifest
import android.content.Context
import java.io.File

class GlobalValues private constructor() {
    companion object {
        fun getSensorRecordingsBaseDir(context: Context): File {
            return context.getExternalFilesDir(null) ?: context.dataDir
        }
        fun getSensorRecordingsTempDir(context: Context): File {
            return  context.dataDir.resolve("temp")
        }
        val requiredPermissions = arrayListOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        )
    }
}
