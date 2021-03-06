package sensors_in_paradise.sonar

import android.Manifest
import android.content.Context
import android.os.Build
import com.xsens.dot.android.sdk.models.XsensDotPayload
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class GlobalValues private constructor() {
    companion object {
        const val NULL_ACTIVITY = "null - activity"
        const val OTHERS_CATEGORY = "Others"
        const val UNKNOWN_PERSON = "unknown"
        const val ACTIVE_RECORDING_FLAG_FILENAME = "active"
        const val METADATA_JSON_FILENAME = "metadata.json"
        const val MEASUREMENT_MODE = XsensDotPayload.PAYLOAD_TYPE_CUSTOM_MODE_4

        fun getSensorRecordingsBaseDir(context: Context): File {
            return context.getExternalFilesDir(null) ?: context.dataDir
        }

        fun getSensorRecordingsTempDir(context: Context): File {
            return context.dataDir.resolve("temp")
        }

        fun getVideoRecordingsTempDir(context: Context): File {
            return context.dataDir.resolve("videoTemp")
        }

        fun getPoseRecordingsTempDir(context: Context): File {
            return context.dataDir.resolve("poseTemp")
        }

        fun getRequiredPermissions(): ArrayList<String> {
            val result = arrayListOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CAMERA
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                result.add(Manifest.permission.BLUETOOTH_SCAN)
                result.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            return result
        }

        val sensorTagPrefixes = listOf("LF", "LW", "ST", "RW", "RF")

        fun formatTag(tagPrefix: String, deviceSetKey: String): String {
            return "$tagPrefix-$deviceSetKey"
        }

        fun getDurationAsString(durationMS: Long): String {

            val diffSecs = (durationMS) / 1000
            val minutes = diffSecs / 60
            val seconds = diffSecs - (diffSecs / 60) * 60

            return minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
        }
        private val fileEmojiMap = mapOf(
            "mp4" to "\uD83C\uDF9E???",
            "json" to "\uD83D\uDCD8",
            "csv" to "\uD83D\uDCCA"
        )

        fun getFileEmoji(file: File): String {
            if (file.isDirectory) {
                return "\uD83D\uDCC1"
            }
            val name = file.name
            val extension = name.substring(name.lastIndexOf(".") + 1)
            return fileEmojiMap[extension] ?: "\uD83D\uDCC4"
        }
    }
}
