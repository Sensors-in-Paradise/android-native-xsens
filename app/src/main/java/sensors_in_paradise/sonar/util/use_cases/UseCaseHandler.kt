package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import android.util.Log
import java.io.File

class UseCaseHandler(
    val context: Context
) {
    private var useCaseStorage = UseCaseStorage(context)
    private val useCasesBaseDir = getUseCasesBaseDir(context)
    private var defaultUseCase = UseCase(context,
        useCasesBaseDir, DEFAULT_USE_CASE_TITLE, useCaseStorage.getSelectedSubDir(
            DEFAULT_USE_CASE_TITLE
        ), this::onRecordingsSubDirOfUseCaseChanged
    )
    val availableUseCases = arrayListOf<UseCase>()
    private var currentUseCase: UseCase = defaultUseCase

    private var onUseCaseChanged: ((useCase: UseCase) -> Unit)? = null

    init {
        loadUseCases()
        setUseCase(useCaseStorage.getSelectedUseCase())
    }

    private fun loadUseCases() {
        val useCaseDirs = useCasesBaseDir.listFiles { dir, name ->
            dir.resolve(name).isDirectory
        }
        Log.d("UseCaseHandler-loadUseCases", "useCaseDirs: ${useCasesBaseDir.listFiles()?.map { it.name }}")
        if (useCaseDirs != null) {
            for (dir in useCaseDirs) {
                availableUseCases.add(
                    UseCase(
                        context,
                        useCasesBaseDir,
                        dir.name,
                        useCaseStorage.getSelectedSubDir(
                            dir.name
                        ), this::onRecordingsSubDirOfUseCaseChanged
                    )
                )
            }
        }
    }

    fun setUseCase(title: String): Boolean {
        val useCase = availableUseCases.find { it.title == title }
        setUseCase(useCase ?: defaultUseCase)
        return useCase != null
    }

    private fun setUseCase(useCase: UseCase) {
        useCaseStorage.setSelectedUseCase(useCase.title)
        currentUseCase = useCase
        onUseCaseChanged?.invoke(useCase)
    }
    fun createUseCase(title: String): UseCase {
            val useCase = UseCase(
                context,
                useCasesBaseDir,
                title,
                onSubdirectoryChanged = this::onRecordingsSubDirOfUseCaseChanged
            )
            availableUseCases.add(useCase)
        return useCase
    }
    fun createAndSetUseCase(title: String) {
        setUseCase(createUseCase(title))
    }

    fun getCurrentUseCase(): UseCase {
        return currentUseCase
    }

    fun hasUseCase(title: String): Boolean {
        return availableUseCases.find { it.title == title } != null
    }

    fun setOnUseCaseChanged(onUseCaseChanged: (useCase: UseCase) -> Unit) {
        this.onUseCaseChanged = onUseCaseChanged
    }

    private fun onRecordingsSubDirOfUseCaseChanged(useCase: UseCase, dir: File) {
        useCaseStorage.setSelectedSubDir(useCase.title, dir.name)
    }

    companion object {
        fun getUseCasesBaseDir(context: Context): File {
            return File(context.getExternalFilesDir(null) ?: context.dataDir, "useCases")
        }

        const val DEFAULT_USE_CASE_TITLE = "default"
    }
}
