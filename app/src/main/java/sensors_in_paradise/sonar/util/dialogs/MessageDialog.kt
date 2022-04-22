package sensors_in_paradise.sonar.util.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class MessageDialog(
    context: Context,
    message: String,
    title: String? = null,
    positiveButtonText: String = "Yes",
    onPositiveButtonClickListener: DialogInterface.OnClickListener? = null,
    neutralButtonText: String = "Neutral",
    onNeutralButtonClickListener: DialogInterface.OnClickListener? = null
) {
    init {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        if (title != null) {
            builder.setTitle(title)
        }
        if (onNeutralButtonClickListener != null) {
            builder.setNeutralButton(neutralButtonText, onNeutralButtonClickListener)
        }

        if (onPositiveButtonClickListener != null) {
            builder.setPositiveButton(
                positiveButtonText,
                onPositiveButtonClickListener
            )
            builder.setNegativeButton("Cancel",
                { dialog, id ->
                    // User cancelled the dialog
                    dialog.cancel()
                })
        } else {
            builder.setPositiveButton(
                "Ok", null
            )
        }
        // Create the AlertDialog object and return it
        builder.create().show()
    }
}
