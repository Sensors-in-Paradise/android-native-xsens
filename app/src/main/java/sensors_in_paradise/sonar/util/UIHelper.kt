package sensors_in_paradise.sonar.util

import android.app.AlertDialog
import android.content.Context

class UIHelper private constructor() {
    companion object {
        fun showAlert(context: Context, message: String) {
            lateinit var disconnectionAlertDialog: AlertDialog
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(true)
            builder.setMessage(message)
            disconnectionAlertDialog = builder.create()
            disconnectionAlertDialog.show()
        }
    }
}
