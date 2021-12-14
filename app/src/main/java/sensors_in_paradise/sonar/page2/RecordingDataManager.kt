package sensors_in_paradise.sonar.page2

import android.util.Log
import java.io.File

class RecordingDataManager(private val filePath: String, private val recordingPreferences: RecordingPreferences) {

    fun getRecordings(): ArrayList<String> {
        val recordingsList = ArrayList<String>()

        File(filePath).walk().forEach {
            // This might have to be discussed
            // Removes all directories from output that don't end with two numbers (millis)
            try {
                it.toString().takeLast(2).toInt()

                Log.d("TEST", it.toString())

                recordingsList.add(it.toString())
            } catch (exception: NumberFormatException) {
                return@forEach // continue
            }
        }

        return recordingsList
    }

    fun getNumberOfRecordings(): Map<String, Int> {
        val recordings = getRecordings()

        val activities = ArrayList<String>()

        for (rec in recordings) {
            activities.add(getActivityFromRecording(rec))
        }

        return activities.groupingBy { it }.eachCount()
    }

    fun getPersonFromRecording(recording: String): String {
        val filePath = File(recording).parentFile!!.toString()
        val index = filePath.lastIndexOf("/")
        val person = recording.slice(IntRange(index + 1, filePath.length - 1))

        return person
    }

    fun getActivityFromRecording(recording: String): String {
        val filePath = File(recording).parentFile!!.parentFile!!.toString()
        val index = filePath.lastIndexOf("/")
        val activity = recording.slice(IntRange(index + 1, filePath.length - 1))

        return activity
    }

    fun getStartingTimeFromRecording(recording: String): String {
        val index = recording.lastIndexOf("/")
        val startTime = recording.slice(IntRange(index + 1, recording.length - 1))

        return startTime
    }

    fun getDurationFromRecording(recording: String): String? {
        return recordingPreferences.getRecordingDuration(recording)
    }

    fun saveDuration(recording: String, duration: String) {
        recordingPreferences.setRecordingDuration(recording, duration)
    }

    fun deleteRecording(fileOrDir: File) {
        val filename = fileOrDir.toString()

        if (fileOrDir.isDirectory()) {
            for (child in fileOrDir.listFiles()) {
                deleteRecording(child)
            }
        }

        fileOrDir.delete()

        recordingPreferences.deleteRecordingDuration(filename)
    }

    fun checkEmptyFiles(fileOrDir: File): Boolean {
        val emptyFileSize = 430

        if (fileOrDir.isDirectory()) {
            for (child in fileOrDir.listFiles()) {
                if (child.length() < emptyFileSize) {
                    return true
                }
            }
        }

        return false
    }
}
