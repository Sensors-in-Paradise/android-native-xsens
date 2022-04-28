package sensors_in_paradise.sonar.util.use_cases

import java.io.File
import java.lang.IllegalStateException

class UseCase(
    baseDir: File,
    val title: String,
    recordingsSubDirName: String = DEFAULT_RECORDINGS_SUB_DIR_NAME,
    private val onSubdirectoryChanged: (useCase: UseCase, dir: File) -> Unit
) {
    val useCaseDir = baseDir.resolve(title).apply { mkdirs() }
    private var recordingsSubDir =
        getRecordingsDir().resolve(recordingsSubDirName).apply { mkdir() }


    fun getActivityLabelsJSONFile(): File {
        return useCaseDir.resolve("labels.json")
    }

    fun getPeopleJSONFile(): File {
        return useCaseDir.resolve("people.json")
    }

    fun getModelFile(): File {
        return useCaseDir.resolve("model.tflite")
    }

    fun getRecordingsDir(): File {
        return useCaseDir.resolve("recordings").apply { mkdir() }
    }

    fun getTrainingHistoryJSONFile(): File {
        return useCaseDir.resolve("trainHistory.json")
    }

    fun getAvailableRecordingSubDirs(): List<String> {
        val subDirs =
            (getRecordingsDir().listFiles { d, name -> File(d, name).isDirectory })?.map { it.name }
        if (subDirs == null) {
            throw IllegalStateException("A use case must at least have the default $DEFAULT_RECORDINGS_SUB_DIR_NAME subdirectory but it has none.")
        }
        return subDirs
    }

    /*fun extractModelFromFile(): Any {
            val modelFile = getUSeCaseBaseDir().resolve(title)
            val inputStream = FileInputStream(modelFile)
            return inputStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length())
        }

        */
    fun setRecordingsSubDir(dir: String) {
        recordingsSubDir = getRecordingsDir().resolve(dir).apply { mkdir() }
        onSubdirectoryChanged(this, recordingsSubDir)
    }

    fun getRecordingsSubDir(): File {
        return recordingsSubDir
    }

    fun getDisplayInfo(): String {
        return "\uD83D\uDCBC " + title + "    \uD83D\uDCC2 " + recordingsSubDir.name
    }

    companion object {
        const val DEFAULT_RECORDINGS_SUB_DIR_NAME = "default"
    }
}
