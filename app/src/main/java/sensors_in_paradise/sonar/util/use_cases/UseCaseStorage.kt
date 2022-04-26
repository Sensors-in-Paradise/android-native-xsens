package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class UseCaseStorage(file: File) : JSONStorage(file) {
    constructor(context: Context) : this(GlobalValues.getUseCaseStorageFile(context))

    companion object {
        private const val SELECTED_USE_CASE_KEY = "selectedUseCase"
    }

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
        return json.getString(SELECTED_USE_CASE_KEY)
    }
}