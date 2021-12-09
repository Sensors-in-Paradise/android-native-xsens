package sensors_in_paradise.sonar

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class ApproveDialog(context: Context, message: String, onPositiveButtonClickListener: DialogInterface.OnClickListener) {
    init {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setPositiveButton("Yes",
                onPositiveButtonClickListener)
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                    dialog.cancel()
                })
        // Create the AlertDialog object and return it
        builder.create().show()
    }
}
