package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import sensors_in_paradise.sonar.GlobalValues
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel


class UseCase(
    val context: Context,
    val title: String
) {
    private var model: File

    init {
        model = if (useCaseAlreadyExists()) {
            if (getUseCaseModel().isFile) getUseCaseModel() else TODO("model needs to be added dialog")
        } else {
            TODO(
                "model needs to be added: if were using the default use case just use some default model," +
                        "for other use cases we might want to choose some from some basic models "
            )
        }
    }

    fun setDefaultModel() {
        val inStream: FileInputStream =
            context.assets.open("LSTMModel-1-18.tflite") as FileInputStream
        val outStream = FileOutputStream(getUseCaseModel())
        val inChannel: FileChannel = inStream.channel
        val outChannel: FileChannel = outStream.channel
        inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()
    }

    private fun getUseCaseModel(): File {
        return GlobalValues.getUseCaseBaseDir(context, title).resolve("model.tflite")
    }


    private fun useCaseAlreadyExists(): Boolean {
        return GlobalValues.getUseCaseBaseDir(context, title).isDirectory
    }

}