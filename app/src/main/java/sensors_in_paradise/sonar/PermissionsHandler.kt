package sensors_in_paradise.sonar

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import sensors_in_paradise.sonar.util.PermissionsHelper
import sensors_in_paradise.sonar.util.UIHelper

class PermissionsHandler(private val requestPermissionLauncher: ActivityResultLauncher<Array<String>>) : PageInterface {
    lateinit var context: Context
    private var dialog: AlertDialog? = null

    override fun activityCreated(activity: Activity) {
        context = activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GlobalValues.requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        val missingPermissions = PermissionsHelper.getRequiredButUngrantedPermissions(context)
        requestPermissionLauncher.launch(missingPermissions.toArray(arrayOf<String>()))
    }

    override fun activityResumed() {
        dialog?.dismiss()
        if (!PermissionsHelper.areAllPermissionsGranted(context)) {
            val missingPermissions = PermissionsHelper.getRequiredButUngrantedPermissions(context)
            dialog = UIHelper.showAlert(
                context,
                missingPermissions.joinToString(separator = "\n\n"),
                title = "Permissions missing",
                cancellable = false
            )
        }
    }
}
