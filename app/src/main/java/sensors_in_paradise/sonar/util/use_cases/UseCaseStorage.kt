package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import android.util.Log
import org.json.JSONObject
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class UseCaseStorage(file: File) : JSONStorage(file) {
    constructor(context: Context) : this(getUseCaseStorageFile(context))

    private val useCases = json.getJSONObject(USE_CASES_KEY)

    override fun onFileNewlyCreated() {
        json.put(SELECTED_USE_CASE_KEY, GlobalValues.DEFAULT_USE_CASE_TITLE)
        val useCases = JSONObject()
        json.put(USE_CASES_KEY, useCases)
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
        /*
            json: {
                selectedUseCase: "",
                useCases: {
                    default:{
                        selectedSubDir: "default",
                    }
                }
            }
        */
        Log.d("useCase", json.toString())
        Log.d("useCase", useCases.toString())
        val useCase = useCases.getJSONObject(useCaseTitle)
        useCase.put(SELECTED_SUB_DIR_KEY, subDir)
        save()
    }

    fun getSelectedSubDir(useCaseTitle: String): String {
        Log.d("useCase", json.toString())
        Log.d("useCase", useCases.toString())
        if (useCases.has(useCaseTitle)) {
            return useCases.getJSONObject(useCaseTitle).optString(SELECTED_SUB_DIR_KEY)
                ?: UseCase.DEFAULT_RECORDINGS_SUB_DIR_NAME
        } else {
            useCases.put(
                useCaseTitle, JSONObject().put(
                    SELECTED_SUB_DIR_KEY, UseCase.DEFAULT_RECORDINGS_SUB_DIR_NAME
                )
            )
            return UseCase.DEFAULT_RECORDINGS_SUB_DIR_NAME
        }
    }

    companion object {
        private const val SELECTED_USE_CASE_KEY = "selectedUseCase"
        private const val USE_CASES_KEY = "useCases"
        private const val SELECTED_SUB_DIR_KEY = "selectedSubDir"
        fun getUseCaseStorageFile(context: Context): File {
            return context.dataDir.resolve("currentUseCase.json")
        }
    }
}
