package sensors_in_paradise.sonar.page2

import android.util.Log
import java.io.File

class RecordingFilesManager(val filePath: String) {

    fun getRecordings(): ArrayList<String> {
        var recordingsList = ArrayList<String>()
        File(filePath).walk().forEach {
            // This might have to be discussed
            // Removes all directories from output that don't end with three numbers (millis)
            try {
                it.toString().takeLast(3).toInt()
                recordingsList.add(it.toString())
            } catch (exception: NumberFormatException) {
                return@forEach // continue
            }
        }

        return recordingsList
    }

    fun getActivityFromRecording(recording: String): String {
        var preIndex = recording.indexOf("/files/")
        var postIndex = recording.lastIndexOf("/")
        var activity = recording.slice(IntRange(preIndex + "/files/".length, postIndex - 1))

        return activity
    }

    fun getStartingTimeFromRecording(recording: String): String {
        var index = recording.lastIndexOf("/")
        var startTime = recording.slice(IntRange(index + 1, recording.length - 1))

        return startTime
    }

    fun deleteRecording(fileOrDir: File) {
        if (fileOrDir.isDirectory())
            for (child in fileOrDir.listFiles()) {
                deleteRecording(child)
            }

        fileOrDir.delete()
    }

}