package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class UseCase(
    val context: Context,
    baseDir: File,
    val title: String,
    recordingsSubDirName: String = DEFAULT_RECORDINGS_SUB_DIR_NAME,
    private val onSubdirectoryChanged: (useCase: UseCase, dir: File) -> Unit,
    private val onUseCaseDeleted: (useCase: UseCase) -> Unit,
    private val onUseCaseDuplicated: (originalUseCase:UseCase, duplicateTitle: String)-> UseCase
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
            throw IllegalStateException(
                "A use case must at least have the default $DEFAULT_RECORDINGS_SUB_DIR_NAME" +
                        " subdirectory but it has none."
            )
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

        val mappedByteBuffer = inputStream.channel.map(
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
    fun delete(){
        try {
            delete(useCaseDir)
        } catch (e: IOException){
            Toast.makeText(context, "Deleting file failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
        onUseCaseDeleted(this)
    }
    fun duplicate(titleOfDuplicate: String): UseCase{
        val targetDir = useCaseDir.parentFile!!.resolve(titleOfDuplicate)
        copyFolder(useCaseDir.toPath(), targetDir.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return onUseCaseDuplicated(this, titleOfDuplicate)
    }

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
                if(file!=null) {
                    Files.copy(file, target.resolve(source.relativize(file)),*options)
                }
                return FileVisitResult.CONTINUE
            }
        })
    }
    @Throws(IOException::class)
    private fun delete(f: File) {
        if (f.isDirectory) {
            val list = f.listFiles()
            if(list!=null) {
                for (c in list) delete(c)
            }
        }
        if (!f.delete()) throw IOException("Failed to delete file: $f")
    }
    companion object {
        const val DEFAULT_RECORDINGS_SUB_DIR_NAME = "default"
    }
}
