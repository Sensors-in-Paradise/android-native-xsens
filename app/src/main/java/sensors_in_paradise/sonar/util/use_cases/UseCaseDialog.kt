package sensors_in_paradise.sonar.util.use_cases

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.EditText
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
            }
        }
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.setButton(Dialog.BUTTON_NEUTRAL, "New Use Case"){
                _,_ ->
                TextInputDialog(context, "Create new use case",hint="Title of use case", promptInterface = {
                    useCaseHandler.createAndSetUseCase(it)
                    dialog.dismiss()
                })
            }
        }
        dialog.show()

    }
}