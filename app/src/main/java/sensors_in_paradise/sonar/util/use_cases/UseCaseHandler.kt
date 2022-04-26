package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import android.widget.Toast
import sensors_in_paradise.sonar.GlobalValues
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class UseCaseHandler(
    val context: Context,
    val onUseCaseChanged: (useCase: UseCase) -> Unit
) {
    private lateinit var useCase: UseCase
    private var useCaseStorage = UseCaseStorage(context)

    init {
        if (!defaultUseCaseExists(context)) {
            createDefaultUseCase()
        } else {
            loadUseCaseFromStorage()
        }
    }

    private fun loadUseCaseFromStorage() {
        val title = useCaseStorage.getSelectedUseCase()
        useCase = UseCase(context, title)
    }

    private fun createDefaultUseCase() {
        useCase = UseCase(context, "default")
        useCaseStorage.setSelectedUseCase("default")
        extractDefaultModel(getUseCaseModel(GlobalValues.DEFAULT_USE_CASE_TITLE))
    }

    private fun defaultUseCaseExists(context: Context): Boolean {
        return GlobalValues.getUseCaseBaseDir(
            context,
            GlobalValues.DEFAULT_USE_CASE_TITLE
        ).isDirectory
    }

    fun setUseCase(case: UseCase): Boolean {
        if (useCaseAlreadyExists(case)) {
            if (getUseCaseModel(case.title).isFile) {
                useCase = case
                onUseCaseChanged(case)
                useCaseStorage.setSelectedUseCase(case.title)
                return true
            } else {
                Toast.makeText(context,
                    "No Model in use Case Folder!", Toast.LENGTH_LONG).show()
            }
        } else {

        }
    }

    private fun extractDefaultModel(destination: File) {
        val inStream: FileInputStream =
            context.assets.open("LSTMModel-1-18.tflite") as FileInputStream
        val outStream = FileOutputStream(destination)
        val inChannel: FileChannel = inStream.channel
        val outChannel: FileChannel = outStream.channel
        inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()
    }

    private fun getUseCaseModel(title: String): File {
        return GlobalValues.getUseCaseBaseDir(context, title).resolve("model.tflite")
    }

    private fun useCaseAlreadyExists(case: UseCase): Boolean {
        return GlobalValues.getUseCaseBaseDir(context, case.title).isDirectory
    }

    fun getTitle(): String {
        return useCase.title
    }

}
