package sensors_in_paradise.sonar.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencesHelper private constructor() {
    companion object {
        fun getSharedPreferences(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
        fun shouldUseDarkMode(context: Context): Boolean {
            return getSharedPreferences(context).getBoolean("darkMode", false)
        }
        fun shouldFollowSystemTheme(context: Context): Boolean {
            return !getSharedPreferences(context).getBoolean("unfollowSystemTheme", true)
        }
    }
}
