package sensors_in_paradise.sonar

import android.Manifest
import android.content.Context
import android.os.Build
import com.xsens.dot.android.sdk.models.XsensDotPayload
import java.io.File

class GlobalValues private constructor() {
    companion object {
        const val NULL_ACTIVITY = "null - activity"
        const val OTHERS_CATEGORY = "Others"
        val DEFINED_ACTIVITIES = linkedMapOf<String, String>(
            NULL_ACTIVITY to OTHERS_CATEGORY,
            "aufräumen" to OTHERS_CATEGORY,
            "aufwischen (Staub)" to OTHERS_CATEGORY,
            "Accessoires (Parfüm) anlegen" to "Morgenpflege",
            "Bett machen" to "Morgenpflege",
            "Bett beziehen" to "Morgenpflege",
            "Haare kämmen" to "Morgenpflege",
            "Hautpflege" to "Morgenpflege",
            "Medikamente geben" to "Morgenpflege",
            "Mundpflege" to "Morgenpflege",
            "Umkleiden" to "Morgenpflege",
            "Gesamtwaschen im Bett" to "Morgenpflege",
            "Essen auf Teller geben" to "Mahlzeiten",
            "Essen reichen" to "Mahlzeiten",
            "Getränke ausschenken" to "Mahlzeiten",
            "Getränk geben" to "Mahlzeiten",
            "Assistieren - aufstehen" to "Assistieren",
            "Patient umlagern (Lagerung)" to "Assistieren",
            "Rollstuhl Transfer" to "Assistieren",
            "Dokumentation" to "Organisation",
            "Medikamente stellen" to "Organisation",
        )
        const val UNKNOWN_PERSON = "unknown"
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

        fun getActivityLabelsJSONFile(context: Context): File {
            return File(context.getExternalFilesDir(null) ?: context.dataDir, "labels.json")
        }

        fun getPeopleJSONFile(context: Context): File {
            return File(context.getExternalFilesDir(null) ?: context.dataDir, "people.json")
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
    }
}
