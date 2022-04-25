package sensors_in_paradise.sonar.util

import android.app.AlertDialog
import android.content.Context
import androidx.core.content.ContextCompat
import sensors_in_paradise.sonar.R

class UIHelper private constructor() {
    companion object {
        fun showAlert(
            context: Context,
            message: String,
            title: String? = null,
            cancellable: Boolean = true
        ): AlertDialog {
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(cancellable)
            builder.setMessage(message)
            if (title != null) {
                builder.setTitle(title)
            }
            val disconnectionAlertDialog = builder.create()
            disconnectionAlertDialog.show()
            return disconnectionAlertDialog
        }

        fun getPrimaryColor(context: Context): Int {
            return if (PreferencesHelper.shouldUseDarkMode(context))
                ContextCompat.getColor(context, R.color.colorPrimaryDark)
            else ContextCompat.getColor(context, R.color.colorPrimary)
        }

        fun getSlightBackgroundContrast(context: Context): Int {
            return ContextCompat.getColor(context, R.color.slightBackgroundContrast)
        }

        fun getBackroundContrast(context: Context): Int {
            return if (PreferencesHelper.shouldUseDarkMode(context))
                ContextCompat.getColor(context, R.color.lightGrey)
                else ContextCompat.getColor(context, R.color.darkGrey)
        }
    }
}
