package sensors_in_paradise.sonar.util.use_cases

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.util.dialogs.TextInputDialog

class UseCaseDialog(context: Context, useCaseHandler: UseCaseHandler) {
    private var selectedUseCase = useCaseHandler.getCurrentUseCase()
    init {
        val availableUseCases = useCaseHandler.availableUseCases

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose a use case")
        builder.setMessage("Each use case stores its own tflite model, labels, people and recordings.")
        val root = LayoutInflater.from(context).inflate(R.layout.usecase_dialog, null)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView_useCases_useCaseDialog)
        val useCasesAdapter = UseCasesAdapter(context, availableUseCases) {
            selectedUseCase = it
        }
        val addUseCaseButton = root.findViewById<ImageButton>(R.id.imageButton_addUseCase_useCaseDialog)
        val addUseCaseEditText = root.findViewById<EditText>(R.id.editText_addNewUseCase_useCaseDialog)

        addUseCaseEditText.apply {
            addTextChangedListener {
                val value = text.toString()
                val len = value.length
                addUseCaseButton.apply {
                    isEnabled = availableUseCases.find { it.title == value } == null && len> 0
                }
        }
        }

        addUseCaseButton.apply {
            isEnabled = false
            setOnClickListener {
                val useCase = useCaseHandler.createUseCase(addUseCaseEditText.text.toString())
                val index = availableUseCases.indexOf(useCase)
                Log.d("UseCaseDialog", "setting selectedIndex of usecases to $index")
                useCasesAdapter.selectedIndex = index
                addUseCaseEditText.setText("")
            }
        }

        recyclerView.adapter = useCasesAdapter
        builder.setView(root)

        builder.setPositiveButton("OK") { _, _ ->
                useCaseHandler.setUseCase(selectedUseCase.title)
        }
        builder.setNegativeButton("Cancel", null)
        builder.setNeutralButton("New Use Case", null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener {
                _ ->
                TextInputDialog(context, "Create new use case", hint = "Title of use case", promptInterface = {
                    useCaseHandler.createAndSetUseCase(it)
                    dialog.dismiss()
                })
            }
            dialog.window
                ?.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                )
        }
        dialog.show()
    }
}