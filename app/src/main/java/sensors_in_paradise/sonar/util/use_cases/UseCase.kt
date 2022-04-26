package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import sensors_in_paradise.sonar.GlobalValues
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel


class UseCase(
    val context: Context,
    val title: String
) {
    val baseDir = File(context.getExternalFilesDir(null) ?: context.dataDir, "useCases").resolve(title)
    fun getActivityLabelsJSONFile(): File {
        return baseDir.resolve("labels.json")
    }
    fun getPeopleJSONFile(): File {
        return baseDir.resolve("people.json")
    }

    fun getModelFile(): File {
        return baseDir.resolve("model.tflite")
    }
    /*fun extractModelFromFile(): Any {
            val modelFile = getUSeCaseBaseDir().resolve(title)
            val inputStream = FileInputStream(modelFile)
            return inputStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length())
        }

        */
}