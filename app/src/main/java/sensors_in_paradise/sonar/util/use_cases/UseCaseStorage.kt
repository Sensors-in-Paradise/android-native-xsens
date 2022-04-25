package sensors_in_paradise.sonar

import java.io.File

class UseCaseStorage(file: File) : JSONStorage(file) {
    companion object {
        private const val SELECTED_USE_CASE_KEY = "selectedUseCase"
    }

    override fun onFileNewlyCreated() {
        json.put(SELECTED_USE_CASE_KEY, "default")
    }

    override fun onJSONInitialized() {
        TODO("Not yet implemented")
    }

    fun setSelectedUseCase(title: String) {
        json.put(SELECTED_USE_CASE_KEY, title)
        save()
    }

    fun getSelectedUseCase(): String {
        return json.getString(SELECTED_USE_CASE_KEY)
    }
}