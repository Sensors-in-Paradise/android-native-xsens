package sensors_in_paradise.sonar.util.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.ViewGroup
import sensors_in_paradise.sonar.custom_views.confusion_matrix.ConfusionMatrixView

class ConfusionMatrixDialog(
    context: Context,
    title: String? = null,
    message: String? = null,
    onPositiveButtonClickListener: DialogInterface.OnClickListener? = null,
    neutralButtonText: String = "Neutral",
    onNeutralButtonClickListener: DialogInterface.OnClickListener? = null
) {

    init {
        val builder = AlertDialog.Builder(context)

        val confusionMatrix = ConfusionMatrixView(context)

        confusionMatrix.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        builder.setView(confusionMatrix)

        if (title != null) {
            builder.setTitle(title)
        }
        if (message != null) {
            builder.setMessage(message)
        }
        if (onNeutralButtonClickListener != null) {
            builder.setNeutralButton(neutralButtonText, onNeutralButtonClickListener)
        }

        if (onPositiveButtonClickListener != null) {
            builder.setPositiveButton(
                "Yes",
                onPositiveButtonClickListener
            )
            builder.setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                // User cancelled the dialog
                dialog.cancel()
            }
        } else {
            builder.setPositiveButton(
                "Ok", null
            )
        }
        builder.setOnDismissListener {
        }
        // Create the AlertDialog object and return it
        builder.create().show()
    }
}
