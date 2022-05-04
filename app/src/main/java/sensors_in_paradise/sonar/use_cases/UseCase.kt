package sensors_in_paradise.sonar.use_cases

import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class UseCase(
    val context: Context,
    baseDir: File,
    val title: String,
    private val onUseCaseDeleted: (useCase: UseCase) -> Unit,
    private val onUseCaseDuplicated: (originalUseCase: UseCase, duplicateTitle: String) -> UseCase,
    recordingsSubDirName: String = DEFAULT_RECORDINGS_SUB_DIR_NAME,
) {
    private val useCaseDir = baseDir.resolve(title).apply { mkdirs() }
    private var recordingsSubDir =
        getRecordingsDir().resolve(recordingsSubDirName).apply { mkdir() }
    private val useCaseStorage = UseCaseStorage(useCaseDir.resolve(STORAGE_SUB_DIR_NAME))

    init {
        useCaseStorage.setSelectedSubDir(recordingsSubDirName)
    }

    fun getActivityLabelsJSONFile(): File {
        return useCaseDir.resolve("labels.json")
    }

    fun getPeopleJSONFile(): File {
        return useCaseDir.resolve("people.json")
    }

    fun getModelFile(): File {
        return useCaseDir.resolve(MODEL_FILE_NAME)
    }

    private fun getRecordingsDir(): File {
        return useCaseDir.resolve("recordings").apply { mkdir() }
    }

    fun getTrainingHistoryJSONFile(): File {
        return useCaseDir.resolve("trainHistory.json")
    }

    fun getAvailableRecordingSubDirs(): List<String> {
        val subDirs =
            (getRecordingsDir().listFiles { d, name -> File(d, name).isDirectory })?.map { it.name }
        if (subDirs == null) {
            return arrayListOf(recordingsSubDir.apply { mkdir() }.name)
        }
        return subDirs
    }

    fun saveModelFromBuffer(buffer: MappedByteBuffer) {
        val modelPath = useCaseDir.resolve(MODEL_FILE_NAME)
        val modelByteArray = ByteArray(buffer.remaining())
        buffer.get(modelByteArray, 0, modelByteArray.size)
        modelPath.writeBytes(modelByteArray)
    }

    fun setRecordingsSubDir(dir: String) {
        recordingsSubDir = getRecordingsDir().resolve(dir).apply { mkdir() }
        useCaseStorage.setSelectedSubDir(dir)
    }

    fun getRecordingsSubDir(): File {
        return recordingsSubDir
    }
    fun importDefaultModel() {
        extractFileFromAssets(context, "LSTMModel-1-18.tflite", getModelFile())
    }

    fun getDisplayInfo(): String {
        return "\uD83D\uDCBC " + title + "    \uD83D\uDCC2 " + recordingsSubDir.name
    }
    fun delete() {
        try {
            delete(useCaseDir)
        } catch (e: IOException) {
            Toast.makeText(context, "Deleting file failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
        onUseCaseDeleted(this)
    }
    fun duplicate(titleOfDuplicate: String): UseCase {
        val targetDir = useCaseDir.parentFile!!.resolve(titleOfDuplicate)
        copyFolder(useCaseDir.toPath(), targetDir.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return onUseCaseDuplicated(this, titleOfDuplicate)
    }

    @Throws(IOException::class)
    private fun delete(f: File) {
        if (f.isDirectory) {
            val list = f.listFiles()
            if (list != null) {
                for (c in list) delete(c)
            }
        }
        if (!f.delete()) throw IOException("Failed to delete file: $f")
    }
    companion object {
        const val DEFAULT_RECORDINGS_SUB_DIR_NAME = "default"
        const val STORAGE_SUB_DIR_NAME = "storage.json"
        const val MODEL_FILE_NAME = "model.tflite"
        fun extractFileFromAssets(context: Context, assetFileName: String, targetFile: File) {
            val iStream = context.assets.open(assetFileName)
            iStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        @Suppress("SpreadOperator")
        @Throws(IOException::class)
        private fun copyFolder(source: Path, target: Path, vararg options: CopyOption?) {
            Files.walkFileTree(source, object : SimpleFileVisitor<Path?>() {
                @Throws(IOException::class)
                override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                    Files.createDirectories(target.resolve(source.relativize(dir)))
                    return FileVisitResult.CONTINUE
                }

                @Throws(IOException::class)
                override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                    if (file != null) {
                        Files.copy(file, target.resolve(source.relativize(file)), *options)
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        }
    }
}
