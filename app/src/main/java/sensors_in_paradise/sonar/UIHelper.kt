package sensors_in_paradise.sonar

import android.app.AlertDialog
import android.content.Context

class UIHelper(private var context: Context) {
    fun buildAndShowAlert(message: String) {
        lateinit var disconnectionAlertDialog: AlertDialog
        val builder = AlertDialog.Builder(this.context)
        builder.setCancelable(true)
        builder.setMessage(message)
        disconnectionAlertDialog = builder.create()
        disconnectionAlertDialog.show()
    }
}
