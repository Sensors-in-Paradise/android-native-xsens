package sensors_in_paradise.sonar.page2

import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class RecordingDataManager(jsonFile: File, val recordingsDir: File) : JSONStorage(jsonFile) {
    private val recordingsList = ArrayList<String>()

    init {
        loadRecordingsFromStorage()
    }

    private fun loadRecordingsFromStorage() {
        recordingsList.clear()
        recordingsDir.walk().forEach {
            // This might have to be discussed
            // Removes all directories from output that don't end with two numbers (millis)
            try {
                it.toString().takeLast(2).toInt()
                recordingsList.add(it.toString())
            } catch (exception: NumberFormatException) {
                return@forEach // continue
            }
        }
    }
    fun getRecordings(): ArrayList<String> {
        return recordingsList
    }
    fun getNumberOfRecordings(): Map<String, Int> {
        val activities = ArrayList<String>()
        for (rec in recordingsList) {
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

        return if (json.has(recording)) json.getString(recording) else "Unknown"
    }

    fun addRecordingAt0(recording: String, duration: String) {
        recordingsList.add(0, recording)
        json.put(recording, duration)
        save()
    }
    fun deleteRecording(fileOrDir: File) {
        val recordingName = fileOrDir.toString()
        deleteRecordingFilesAndDirs(fileOrDir)
        json.remove(recordingName)
        recordingsList.remove(recordingName)
        save()
    }
    private fun deleteRecordingFilesAndDirs(fileOrDir: File) {
        if (fileOrDir.isDirectory()) {
            for (child in fileOrDir.listFiles()) {
                deleteRecordingFilesAndDirs(child)
            }
        }
        fileOrDir.delete()
    }

    fun checkEmptyFiles(fileOrDir: File): Boolean {
        val emptyFileSize = 430

        if (fileOrDir.isDirectory) {
            for (child in fileOrDir.listFiles()) {
                if (child.length() < emptyFileSize) {
                    return true
                }
            }
        }

        return false
    }

    override fun onFileNewlyCreated() {}

    override fun onJSONInitialized() {}
}
