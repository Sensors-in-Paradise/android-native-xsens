package sensors_in_paradise.sonar.util.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.VideoView
import java.io.File

class VideoDialog(
    context: Context,
    videoFile: File,
    title: String? = null,
    message: String? = null,
    onPositiveButtonClickListener: DialogInterface.OnClickListener? = null,
    neutralButtonText: String = "Neutral",
    onNeutralButtonClickListener: DialogInterface.OnClickListener? = null
) {
    init {
        val builder = AlertDialog.Builder(context)
        val videoView = VideoView(context).apply {
            setVideoPath(videoFile.absolutePath)
            setOnPreparedListener { mp -> mp.isLooping = true }
            start()
        }

        builder.setView(videoView)
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
            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id ->
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
