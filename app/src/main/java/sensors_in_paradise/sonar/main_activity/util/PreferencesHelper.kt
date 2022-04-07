package sensors_in_paradise.sonar.main_activity.util

import android.content.Context
import android.content.SharedPreferences
import androidx.camera.video.Quality
import androidx.preference.PreferenceManager
import sensors_in_paradise.sonar.R

class PreferencesHelper private constructor() {
    companion object {
        fun getSharedPreferences(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun shouldUseDarkMode(context: Context): Boolean {
            return getSharedPreferences(context).getBoolean("darkMode", false)
        }

        fun shouldFollowSystemTheme(context: Context): Boolean {
            return !getSharedPreferences(context).getBoolean("unfollowSystemTheme", false)
        }

        fun shouldShowToastsVerbose(context: Context): Boolean {
            return getSharedPreferences(context).getBoolean("verboseToasts", false)
        }

        fun getRecordingsSubDir(context: Context): String {
            return getSharedPreferences(context).getString(
                "recordingsSubDir",
                context.getString(R.string.default_recordings_subdir)
            )!!
        }
        fun getWebDAVUrl(context: Context): String {
            val url = getSharedPreferences(context).getString(
                "cloudBaseURL",
                context.getString(R.string.default_webdav_cloud_url)
            )!!
            if (url.endsWith("/")) {
                return url
            }
            return "$url/"
        }
        fun getWebDAVUser(context: Context): String {
            return getSharedPreferences(context).getString(
                "cloudWebDAVUserName",
                context.getString(R.string.default_webdav_username)
            )!!
        }
        fun getWebDAVToken(context: Context): String {
            return getSharedPreferences(context).getString(
                "cloudWebDavToken",
                ""
            )!!
        }
        fun shouldRecordWithCamera(context: Context): Boolean {
            return getSharedPreferences(context).getBoolean("recordWithCamera", false)
        }
        fun shouldStoreRawCameraRecordings(context: Context): Boolean {
            return getSharedPreferences(context).getBoolean("storeRawCameraVideo", false)
        }
        fun getCameraRecordingQuality(context: Context): Quality {
            return when (getSharedPreferences(context).getString("videoRecordingQuality", "LOWEST")) {
                "HIGHEST" -> Quality.HIGHEST
                "UHD" -> Quality.UHD
                "FHD" -> Quality.FHD
                "HD" -> Quality.HD
                "SD" -> Quality.SD
                "LOWEST" -> Quality.LOWEST
                else -> Quality.LOWEST
            }
        }
    }
}
