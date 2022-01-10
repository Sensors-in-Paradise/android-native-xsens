package sensors_in_paradise.sonar.page2

import sensors_in_paradise.sonar.JSONStorage
import java.io.BufferedReader
import sensors_in_paradise.sonar.GlobalValues
import java.io.File
import java.io.FileReader
import kotlin.random.Random


class RecordingDataManager(private val recordingsDir: File) {
    val recordingsList = ArrayList<Recording>()

    init {
        loadRecordingsFromStorage()
    }

    private fun loadRecordingsFromStorage() {
        recordingsList.clear()

        recordingsDir.walk().forEach {
            if (it.isDirectory) {
                if (isRecordingDir(it)) {
                    recordingsList.add(Recording(it))
                }
            }
        }
    }
    private fun isRecordingDir(file: File): Boolean {
        val childDirs = file.listFiles { dir, filename -> dir.resolve(filename).isDirectory }
        if (childDirs == null || childDirs.isEmpty()) {
            val metadataFile = file.resolve(GlobalValues.METADATA_JSON_FILENAME)
            return metadataFile.exists()
        }
        return false
    }
    fun getNumberOfRecordingsPerActivity(): Map<String, Int> {
        val activities = ArrayList<String>()
        for (rec in recordingsList) {
            val storage = rec.metadataStorage
            activities.addAll(storage.getActivities().map { (_, label) -> label })
        }
        return activities.groupingBy { it }.eachCount()
    }

    fun deleteRecording(recording: Recording) {
        deleteRecordingFilesAndDirs(recording.dir)
        recordingsList.remove(recording)
    }

    private fun deleteRecordingFilesAndDirs(fileOrDir: File) {
        if (fileOrDir.isDirectory) {
            val children = fileOrDir.listFiles()
            if (children != null) {
                for (child in children) {
                    deleteRecordingFilesAndDirs(child)
                }
            }
        }
        fileOrDir.delete()
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
                reader.close()
                return columns[1]
            }

            counter++
        }

        reader.close()
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
                    reader.close()
                    return true
                }
            }
            headerSize--
        }

        reader.close()
        return false
    }

    fun checkSynchronizedTimeStamps(fileOrDir: File): Boolean {
        val headerSize = 9
        val margin = 10

        if (fileOrDir.isDirectory) {
            val childCSVs = fileOrDir.listFiles { _, name -> name.endsWith(".csv") }
            if (childCSVs != null) {
                val firstFile = childCSVs[0]

                val lineNumber = getAbsoluteLineNumber(firstFile)
                val randomLine = Random.nextInt(headerSize + margin, lineNumber - margin)
                val timestamp = getTimeStampAtLine(firstFile, randomLine)

                assert(timestamp != "") { "No initial timestamp could be found." }

                for (child in childCSVs) {
                    if (!findTimeStamp(child, timestamp)) {
                        return false
                    }
                }
            }
        }

        return true
    }
}
