package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class UseCaseHandler(
    val context: Context

) {
    private val useCasesBaseDir = getUseCasesBaseDir(context)
    private var defaultUseCase = UseCase(useCasesBaseDir, DEFAULT_USE_CASE_TITLE)
    val availableUseCases = ArrayList<UseCase>()
    private var currentUseCase: UseCase = defaultUseCase
    private var useCaseStorage = UseCaseStorage(context)
    private var onUseCaseChanged: ((useCase: UseCase) -> Unit)? = null
    init {
        loadUseCases()
        setUseCase(useCaseStorage.getSelectedUseCase())
    }
    private fun loadUseCases(){
        val useCaseDirs = useCasesBaseDir.listFiles { dir,name ->
            dir.resolve(name).isDirectory }
        if(useCaseDirs!=null) {
            for (dir in useCaseDirs) {
                availableUseCases.add(UseCase(useCasesBaseDir, dir.name))
            }
        }
    }
    fun setUseCase(title: String): Boolean {
        val useCase = availableUseCases.find { it.title == title }
        setUseCase(useCase ?: defaultUseCase)
        return useCase!=null
    }
    private fun setUseCase(useCase: UseCase) {
        useCaseStorage.setSelectedUseCase(useCase.title)
        currentUseCase=useCase
        onUseCaseChanged?.invoke(useCase)
    }

    fun createAndSetUseCase(title: String) {
        val useCase = UseCase(useCasesBaseDir,title)
        availableUseCases.add(useCase)
        setUseCase(useCase)
    }
    fun getCurrentUseCase(): UseCase{
        return currentUseCase
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

    private fun hasUseCase(title:String): Boolean {
        return availableUseCases.find { it.title ==title } != null
    }
    fun setOnUseCaseChanged(onUseCaseChanged: (useCase: UseCase)->Unit){
        this.onUseCaseChanged = onUseCaseChanged
    }

    companion object{
       fun getUseCasesBaseDir(context: Context):File{
           return File(context.getExternalFilesDir(null) ?: context.dataDir, "useCases")
       }
        const val DEFAULT_USE_CASE_TITLE = "default"
    }
}
