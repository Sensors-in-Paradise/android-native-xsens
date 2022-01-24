package sensors_in_paradise.sonar

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class MessageDialog(context: Context, message: String, onPositiveButtonClickListener: DialogInterface.OnClickListener?=null) {
    init {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        if(onPositiveButtonClickListener!=null) {
            builder.setPositiveButton(
                "Yes",
                onPositiveButtonClickListener
            )
            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                    dialog.cancel()
                })
        }
        else{
            builder.setPositiveButton(
                "Ok", null
            )
        }
        // Create the AlertDialog object and return it
        builder.create().show()
    }
}
