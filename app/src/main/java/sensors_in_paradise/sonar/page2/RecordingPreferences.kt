package sensors_in_paradise.sonar.page2

import android.content.Context

class RecordingPreferences(context: Context) {

    val PREFERENCE_NAME = "RecordingPreferences"

    val preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun getRecordingDuration(recordingID: String): String? {
        return preference.getString(recordingID, "No duration found.")
    }

    fun setRecordingDuration(recordingID: String, duration: String) {
        val editor = preference.edit()
        editor.putString(recordingID, duration).commit()
    }

    fun deleteRecordingDuration(recordingID: String) {
        val editor = preference.edit()
        editor.remove(recordingID).commit()
    }
}
