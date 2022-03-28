package sensors_in_paradise.sonar.custom_views.stickman

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.custom_views.stickman.object3d.Plane
import sensors_in_paradise.sonar.custom_views.stickman.object3d.Stickman

class StickmanDialog(context: Context) {
    private var dialog: AlertDialog

    init {

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Stickman")

        val root = LayoutInflater.from(context).inflate(R.layout.stickman_dialog, null)
        val stickmanView = root.findViewById<Render3DView>(R.id.stickmanView)
        stickmanView.apply {
            addObject3D(Plane().apply { scale(1f, 0f, 1f) })
            addObject3D(Stickman())
        }

        stickmanView.camera.center.apply {
            x = 0f
            y = 0.5f
            z = 0f
        }
        stickmanView.camera.eye.apply {
            x = 0f
            y = 1f
            z = -2f
        }
        stickmanView.camera.up.apply {
            x = 0f
            y = 1f
            z = 0f
        }
        stickmanView.camera.notifyCameraChanged()
        stickmanView.onObjectChanged()
        stickmanView.enableYRotation = true
        stickmanView.showFPS = true
        builder.setView(root)
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        dialog = builder.create()
        dialog.show()
    }
}
