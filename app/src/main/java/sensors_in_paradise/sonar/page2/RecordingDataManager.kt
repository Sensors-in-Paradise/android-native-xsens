package sensors_in_paradise.sonar.page2

import android.util.Log
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class RecordingDataManager(val recordingsDir: File) {
    val recordingsList = ArrayList<Pair<File, RecordingMetadataStorage>>()

    init {
        loadRecordingsFromStorage()
    }

    private fun loadRecordingsFromStorage() {
        recordingsList.clear()

        recordingsDir.walk().forEach {
            if(it.isDirectory) {
                val childDirs = it.listFiles { dir, filename -> dir.resolve(filename).isDirectory }
                if (childDirs == null || childDirs.isEmpty()) {
                    val metadataFile = it.resolve(GlobalValues.METADATA_JSON_FILENAME)
                    val hasMetadataJson = metadataFile.exists()
                        if(hasMetadataJson){
                            recordingsList.add(Pair(it, RecordingMetadataStorage(metadataFile)))
                        }
                }
            }
        }
    }

    fun getNumberOfRecordingsPerActivity(): Map<String, Int> {
        val activities = ArrayList<String>()
        for (rec in recordingsList) {
            activities.addAll(rec.second.getActivities().map{(_, label)-> label})
        }
        return activities.groupingBy { it }.eachCount()
    }
    fun deleteRecording(recordingDir:File){
        deleteRecordingFilesAndDirs(recordingDir)
        val recording = recordingsList.find{(dir, _)-> dir.absolutePath == recordingDir.absolutePath}
        recordingsList.remove(recording)
    }

    fun deleteRecording(recording: Pair<File, RecordingMetadataStorage>){
        deleteRecordingFilesAndDirs(recording.first)
        recordingsList.remove(recording)
    }
    private fun deleteRecordingFilesAndDirs(fileOrDir: File) {
        if (fileOrDir.isDirectory()) {
            val children = fileOrDir.listFiles()
           if(children!=null){
               for (child in children) {
                   deleteRecordingFilesAndDirs(child)
               }
           }
        }
        fileOrDir.delete()
    }

    fun checkEmptyFiles(fileOrDir: File): Boolean {
        val emptyFileSize = 430

        if (fileOrDir.isDirectory) {
            val childCSVs = fileOrDir.listFiles { _, name -> name.endsWith(".csv") }
           if(childCSVs!=null){
               for (child in childCSVs) {
                   if (child.length() < emptyFileSize) {
                       return true
                   }
               }
           }
        }

        return false
    }

}
