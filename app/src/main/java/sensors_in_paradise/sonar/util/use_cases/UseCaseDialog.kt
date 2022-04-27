package sensors_in_paradise.sonar.util.use_cases

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import sensors_in_paradise.sonar.util.dialogs.TextInputDialog

class UseCaseDialog(context: Context, useCaseHandler: UseCaseHandler) {
    init{
        val availableUseCases = useCaseHandler.availableUseCases
        val titles = availableUseCases.map { it.title }.toTypedArray()


        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose a use case")

        var itemIndex = titles.indexOf(useCaseHandler.getCurrentUseCase().title)

        builder.setSingleChoiceItems(
            titles, itemIndex
        ) { _, which ->
            itemIndex = which
        }
        builder.setPositiveButton("OK") { _, _ ->
            if (itemIndex != -1) {
                useCaseHandler.setUseCase(titles[itemIndex])
                UseCaseSubDirDialog(context, useCaseHandler)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.setNeutralButton("New Use Case", null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener{
                _ ->
                TextInputDialog(context, "Create new use case",hint="Title of use case", promptInterface = {
                    useCaseHandler.createAndSetUseCase(it)
                    dialog.dismiss()
                })
            }
        }
        dialog.show()

    }
}