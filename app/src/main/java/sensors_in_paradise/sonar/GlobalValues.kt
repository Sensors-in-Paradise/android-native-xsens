package sensors_in_paradise.sonar

import android.Manifest
import android.content.Context
import java.io.File

class GlobalValues private constructor() {
    companion object {
        const val NULL_ACTIVITY = "null - activity"
        const val UNKNOWN_PERSON = "unknown"
        fun getSensorRecordingsBaseDir(context: Context): File {
            return context.getExternalFilesDir(null) ?: context.dataDir
        }
        fun getSensorRecordingsTempDir(context: Context): File {
            return context.dataDir.resolve("temp")
        }
        fun getActivityLabelsJSONFile(context: Context): File {
            return File(context.dataDir, "labels.json")
        }
        fun getPeopleJSONFile(context: Context): File {
            return File(context.dataDir, "people2.json")
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
        val sensorTagMap = mapOf(
            "LF" to "D4:22:CD:00:06:7B",
            "LW" to "D4:22:CD:00:06:89",
            "ST" to "D4:22:CD:00:06:7F",
            "RW" to "D4:22:CD:00:06:7D",
            "RF" to "D4:22:CD:00:06:72"
        )
        fun sensorAddressToTag(address: String): String {
            return sensorTagMap.filterValues { it == address }.keys.first()
        }
    }
}
