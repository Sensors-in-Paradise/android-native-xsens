package sensors_in_paradise.sonar.util.use_cases

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import sensors_in_paradise.sonar.util.dialogs.TextInputDialog
import java.io.File

class UseCaseSubDirDialog(context: Context, useCaseHandler: UseCaseHandler) {

    init {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Choose a subdirectory")
        val dir = useCaseHandler.getCurrentUseCase().useCaseDir
        val subDirs = (dir.list { d, name -> File(d, name).isDirectory })?.map { it.toString() }
            ?.toTypedArray()
        var checkedItem: Int = -1
        builder.setSingleChoiceItems(subDirs, -1) { _, which -> checkedItem = which }

        builder.setPositiveButton("OK")
        { _, _ ->
            if (checkedItem != -1) {
                useCaseHandler.setSubDir(
                    subDirs?.get(checkedItem) ?: UseCaseHandler.DEFAULT_SUB_DIR_TITLE
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
                        useCaseHandler.createAndSetSubDir(it)
                        dialog.dismiss()
                    })
            }
        }
        dialog.show()


    }
}