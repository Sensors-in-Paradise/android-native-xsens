package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class UseCase(
    val context: Context,
    baseDir: File,
    val title: String,
    recordingsSubDirName: String = DEFAULT_RECORDINGS_SUB_DIR_NAME,
    private val onSubdirectoryChanged: (useCase: UseCase, dir: File) -> Unit
) {
    private val useCaseDir = baseDir.resolve(title).apply { mkdirs() }
    private var recordingsSubDir =
        getRecordingsDir().resolve(recordingsSubDirName).apply { mkdir() }


    fun getActivityLabelsJSONFile(): File {
        return useCaseDir.resolve("labels.json")
    }

    fun getPeopleJSONFile(): File {
        return useCaseDir.resolve("people.json")
    }

    fun getModelFile(): File {
        if (!useCaseDir.resolve("model.tflite").isFile) {
            extractModelFromFile()?.let { saveModelFromFile(it) }
        }
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

    fun extractModelFromFile(filename: String = "LSTMModel-1-18.tflite"): MappedByteBuffer? {
        val iStream = context.assets.open(filename)
        val file = createTempFile()
        iStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val inputStream = FileInputStream(file)

        val mappedByteBuffer =  inputStream.channel.map(
            FileChannel.MapMode.READ_ONLY, 0,
            file.length()
        )
        file.delete()
        return mappedByteBuffer
    }

    private fun saveModelFromFile(buffer: MappedByteBuffer) {
        val modelPath = useCaseDir.resolve("model.tflite")
        val modelByteArray = ByteArray(buffer.remaining())
        buffer.get(modelByteArray, 0, modelByteArray.size)
        modelPath.writeBytes(modelByteArray)
    }

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
