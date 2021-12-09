package sensors_in_paradise.sonar.page2

import android.content.Context

class RecordingPreferences(context: Context) {

    val preferenceName = "RecordingPreferences"

    val preference = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)

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
