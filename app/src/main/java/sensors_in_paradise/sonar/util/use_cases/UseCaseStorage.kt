package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class UseCaseStorage(file: File) : JSONStorage(file) {
    constructor(context: Context) : this(getUseCaseStorageFile(context))

    override fun onFileNewlyCreated() {
        json.put(SELECTED_USE_CASE_KEY, GlobalValues.DEFAULT_USE_CASE_TITLE)
    }

    override fun onJSONInitialized() {
    }

    fun setSelectedUseCase(title: String) {
        json.put(SELECTED_USE_CASE_KEY, title)
        save()
    }

    fun getSelectedUseCase(): String {
        return json.optString(SELECTED_USE_CASE_KEY) ?: UseCaseHandler.DEFAULT_USE_CASE_TITLE
    }

    fun setSelectedSubDir(useCaseTitle: String, subDir: String) {
        // TODO
        //json.put(SELECTED_SUB_DIR_KEY, subDir)
        save()
    }

    fun getSelectedSubDir(useCaseTitle: String): String {
        return json.optString(SELECTED_SUB_DIR_KEY) ?: UseCase.DEFAULT_RECORDINGS_SUBDIR_NAME
    }

    companion object {
        private const val SELECTED_USE_CASE_KEY = "selectedUseCase"
        private const val SELECTED_SUB_DIR_KEY = "selectedSubDir"
        fun getUseCaseStorageFile(context: Context): File {
            return context.dataDir.resolve("currentUseCase.json")
        }
    }
}
