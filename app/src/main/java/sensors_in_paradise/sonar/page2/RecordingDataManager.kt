package sensors_in_paradise.sonar.page2

import sensors_in_paradise.sonar.JSONStorage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.random.Random

class RecordingDataManager(jsonFile: File, private val recordingsDir: File) : JSONStorage(jsonFile) {
    private val recordingsList = ArrayList<String>()

    init {
        loadRecordingsFromStorage()
    }

    private fun loadRecordingsFromStorage() {
        recordingsList.clear()
        recordingsDir.walk().forEach {
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
        if (fileOrDir.isDirectory) {
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

    private fun getAbsoluteLineNumber(file: File): Int {
        var lineNumber = 0

        val reader = BufferedReader(FileReader(file))
        while (reader.readLine() != null) lineNumber++
        reader.close()

        return lineNumber
    }

    private fun getTimeStampAtLine(file: File, lineNumber: Int): String {
        var counter = 0

        val reader = BufferedReader(FileReader(file))
        var line: String
        while (true) {
            line = reader.readLine() ?: break

            if (counter == lineNumber) {
                val columns = line.split(",")
                return columns[1]
            }

            counter++
        }

        return ""
    }

    private fun findTimeStamp(file: File, timestamp: String): Boolean {
        var headerSize = 9

        val reader = BufferedReader(FileReader(file))
        var line: String
        while (true) {
            line = reader.readLine() ?: break

            if (headerSize <= 0) {
                val columns = line.split(",")
                if (columns[1] == timestamp) {
                    return true
                }
            }
            headerSize--
        }

        return false
    }

    fun checkSynchronizedTimeStamps(fileOrDir: File): Boolean {
        val headerSize = 9
        val margin = 10

        if (fileOrDir.isDirectory) {
            val firstFile = fileOrDir.listFiles()[0]

            val lineNumber = getAbsoluteLineNumber(firstFile)
            val randomLine = Random.nextInt(headerSize + margin, lineNumber - margin)
            val timestamp = getTimeStampAtLine(firstFile, randomLine)

            assert(timestamp != "") { "No initial timestamp could be found." }

            for (child in fileOrDir.listFiles()) {
                if (!findTimeStamp(child, timestamp)) {
                    return false
                }
            }
        }

        return true
    }

    override fun onFileNewlyCreated() {}

    override fun onJSONInitialized() {}
}
