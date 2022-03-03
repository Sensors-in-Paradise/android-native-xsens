package sensors_in_paradise.sonar.util

import android.content.Context
import android.content.SharedPreferences
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
    }
}
