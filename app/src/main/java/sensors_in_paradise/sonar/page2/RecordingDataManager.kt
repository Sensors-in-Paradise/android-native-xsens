package sensors_in_paradise.sonar.page2

import sensors_in_paradise.sonar.GlobalValues
import java.io.File

class RecordingDataManager(private val recordingsDir: File, val recordingsList: ArrayList<Recording>) {
    constructor(recordingsDir: File) : this(recordingsDir, ArrayList<Recording>()) {
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
        recording.delete()
        recordingsList.remove(recording)
    }
}
