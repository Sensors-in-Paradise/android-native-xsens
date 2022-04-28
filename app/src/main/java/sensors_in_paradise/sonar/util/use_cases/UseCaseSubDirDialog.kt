package sensors_in_paradise.sonar.util.use_cases

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import sensors_in_paradise.sonar.util.dialogs.TextInputDialog
import java.io.File

class UseCaseSubDirDialog(context: Context, currentUseCase: UseCase) {

    init {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Choose a subdirectory")
        val dir = currentUseCase.useCaseDir
        val subDirs = (dir.list { d, name -> File(d, name).isDirectory })?.map { it.toString() }
            ?.toTypedArray()
        var checkedItem: Int = -1
        builder.setSingleChoiceItems(subDirs, -1) { _, which -> checkedItem = which }

        builder.setPositiveButton("OK") { _, _ ->
            if (checkedItem != -1) {
                currentUseCase.setRecordingsSubDir(
                    subDirs?.get(checkedItem) ?: UseCase.DEFAULT_RECORDINGS_SUB_DIR_NAME
                )
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.setNeutralButton("New subdirectory", null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener { _ ->
                TextInputDialog(
                    context,
                    "Create new Subdirectory",
                    hint = "Title of subdirectory",
                    promptInterface = {
                        currentUseCase.setRecordingsSubDir(it)
                        dialog.dismiss()
                    })
            }
        }
        dialog.show()
    }
}
