package sensors_in_paradise.sonar.page2

import sensors_in_paradise.sonar.GlobalValues
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.math.floor
import kotlin.random.Random

const val XSENS_HEADER_SIZE = 9
const val XSENS_EMPTY_FILE_SIZE = 435

/**
 * Enum that describes the condition / state a recording can be in
 */
enum class RecordingFileState {
    Empty,
    Unsynchronized,
    Valid
}

open class Recording(val dir: File, val metadataStorage: RecordingMetadataStorage) {
    constructor(dir: File) : this(
        dir,
        RecordingMetadataStorage(dir.resolve(GlobalValues.METADATA_JSON_FILENAME))
    )
    constructor(recording: Recording) : this(
        recording.dir,
        recording.metadataStorage
    )
    val state = computeRecordingState()
    val isValid
        get() = state != RecordingFileState.Empty

    private fun areFilesEmpty(dir: File): Boolean {
        val childCSVs = dir.listFiles { _, name -> name.endsWith(".csv") }
        if (childCSVs != null && childCSVs.isNotEmpty()) {
            for (child in childCSVs) {
                if (child.length() < XSENS_EMPTY_FILE_SIZE) {
                    return true
                }
            }
            return false
        }
        return true
    }
    fun getDisplayTitle(): String {
        val numActivities = metadataStorage.getActivities().size
        var result = ""
        if (hasVideoRecording()) {
            result += "\uD83D\uDCF9 "
        }
        result += "$numActivities ${if (numActivities == 1) "activity" else "activities"}"
        return result
    }
    fun getDirectory(): File {
        return dir
    }

    fun delete() {
            val children = dir.listFiles()
            if (children != null) {
                for (child in children) {
                    child.delete()
                }
            }
        dir.delete()
    }

    fun hasVideoRecording(): Boolean {
        return getVideoFile().exists()
    }
    fun getVideoFile(): File {
        return dir.resolve(VIDEO_CAPTURE_FILENAME)
    }

    private fun computeRecordingState(): RecordingFileState {
        var state = checkCache()
        if (state != null) return state

        state = if (areFilesEmpty(dir)) {
            RecordingFileState.Empty
        } else if (!areFilesSynchronized()) {
            RecordingFileState.Unsynchronized
        } else {
            RecordingFileState.Valid
        }

        saveCache(state)
        return state
    }

    /**
     * Checks if the files of the recording are synchronized -> contain exactly the same timestamps.
     * **This only works if the files are not empty.**
     *
     * This is checked on one sample line, as the timestamps are usually consistently spaced for one
     * sensor.
     */
    private fun areFilesSynchronized(): Boolean {
        val childCSVs = dir.listFiles { _, name -> name.endsWith(".csv") } ?: return false

        val firstFile = childCSVs[0]

        val lineNumber = getAbsoluteLineNumber(firstFile)
        val timesteps = lineNumber - XSENS_HEADER_SIZE

        val margin = floor(timesteps * 0.2).toInt()
        val lineFrom = XSENS_HEADER_SIZE + margin
        val lineTo = lineNumber - margin
        // End is exclusive, so +1 on last line
        val randomLine = Random.nextInt(lineFrom, lineTo + 1)
        val timestamp = getTimeStampAtLine(firstFile, randomLine)

        assert(timestamp != "") { "No initial timestamp could be found." }

        for (child in childCSVs) {
            if (!findTimeStamp(child, timestamp)) {
                return false
            }
        }

        return true
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
            } else {
                headerSize--
            }
        }

        reader.close()
        return false
    }

    /**
     * Counts all lines in the given recording file **including** the header
     */
    private fun getAbsoluteLineNumber(file: File): Int {
        var lineNumber = 0

        val reader = BufferedReader(FileReader(file))
        while (reader.readLine() != null) lineNumber++
        reader.close()

        return lineNumber
    }

    fun getRecordingFiles(): Array<File> {
        return dir.listFiles { file -> file.isFile && file.name.endsWith(".csv") } ?: emptyArray()
    }

    private fun checkCache(): RecordingFileState? {
        val state = metadataStorage.getRecordingState()
        if (state == null) return state

        return when (state) {
            "Valid" -> {
                RecordingFileState.Valid
            }
            "Empty" -> {
                RecordingFileState.Empty
            }
            else -> {
                RecordingFileState.Unsynchronized
            }
        }
    }

    private fun saveCache(state: RecordingFileState) {
        val recordingState = when (state) {
            RecordingFileState.Valid -> {
                "Valid"
            }
            RecordingFileState.Empty -> {
                "Empty"
            }
            else -> {
                "Unsynchronized"
            }
        }

        metadataStorage.setRecordingState(recordingState)
    }

    companion object {
        const val VIDEO_CAPTURE_FILENAME = "recording.mp4"
    }
}
