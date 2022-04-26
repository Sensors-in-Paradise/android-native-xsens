package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import sensors_in_paradise.sonar.GlobalValues
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class UseCaseHandler(
    val context: Context,
    val onUseCaseChanged: (useCase: UseCase) -> Unit
) {
    lateinit var currentUseCase: UseCase
    private var useCaseStorage = UseCaseStorage(context)

    init {
        if (!defaultUseCaseExists(context)) {
            createDefaultUseCase()
        } else {
            loadUseCaseFromStorage()
        }
    }

    fun setUseCase(case: UseCase) {
        if (useCaseAlreadyExists(case)) {
            currentUseCase = case
            onUseCaseChanged(case)
            useCaseStorage.setSelectedUseCase(case.title)
        }
    }

    fun createUseCase() {

    }


    private fun loadUseCaseFromStorage() {
        val title = useCaseStorage.getSelectedUseCase()
        currentUseCase = UseCase(context, title)
    }

    private fun createDefaultUseCase() {
        currentUseCase = UseCase(context, GlobalValues.DEFAULT_USE_CASE_TITLE)
        useCaseStorage.setSelectedUseCase(GlobalValues.DEFAULT_USE_CASE_TITLE)
        getDir().mkdir()
        onUseCaseChanged(currentUseCase)
        //extractDefaultModel(useCase.getModelFile())
    }

    private fun defaultUseCaseExists(context: Context): Boolean {
        return File(
            context.getExternalFilesDir(null) ?: context.dataDir,
            "useCases"
        ).resolve(GlobalValues.DEFAULT_USE_CASE_TITLE).isDirectory
    }

    private fun extractDefaultModel(destination: File) {
        val inStream: FileInputStream =
            context.assets.open("LSTMModel118") as FileInputStream
        val outStream = FileOutputStream(destination)
        val inChannel: FileChannel = inStream.channel
        val outChannel: FileChannel = outStream.channel
        inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()
    }

    private fun useCaseAlreadyExists(case: UseCase): Boolean {
        return case.baseDir.isDirectory
    }

    fun getTitle(): String {
        return currentUseCase.title
    }

    fun getDir(): File {
        return currentUseCase.baseDir
    }

}
