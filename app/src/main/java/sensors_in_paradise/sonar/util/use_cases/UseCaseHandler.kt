package sensors_in_paradise.sonar.util.use_cases

import android.content.Context
import sensors_in_paradise.sonar.GlobalValues

class UseCaseHandler(context: Context) {
    private lateinit var useCase: UseCase

    init {
        if (!defaultUseCaseExists(context)) {
            useCase = createDefaultUseCase(context)
        }
    }

    private fun createDefaultUseCase(context: Context): UseCase {
        useCase = UseCase(context, "default")
        return useCase
    }

    private fun defaultUseCaseExists(context: Context): Boolean {
        return GlobalValues.getUseCaseBaseDir(
            context,
            GlobalValues.DEFAULT_USE_CASE_TITLE
        ).isDirectory
    }
}