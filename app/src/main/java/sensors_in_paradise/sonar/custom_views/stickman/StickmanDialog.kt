package sensors_in_paradise.sonar.custom_views.stickman

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import sensors_in_paradise.sonar.R

class StickmanDialog(context: Context) {
    private var dialog: AlertDialog

    init {

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Stickman")

        val root = LayoutInflater.from(context).inflate(R.layout.stickman_dialog, null)
        // val stickmanView = root.findViewById<StickmanView>(R.id.editText_promptDialog)

        builder.setView(root)

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        dialog = builder.create()
        dialog.show()
    }
}
