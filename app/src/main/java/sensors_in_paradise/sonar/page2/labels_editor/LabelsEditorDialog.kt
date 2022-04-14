package sensors_in_paradise.sonar.page2.labels_editor

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.slider.RangeSlider
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page2.Recording

class LabelsEditorDialog(
    context: Context,
    recording: Recording
) {
    init {
        val builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.label_editor, null)
        val rangeSlider = root.findViewById<RangeSlider>(R.id.rangeSlider_labelEditor)

        rangeSlider.values = listOf(10f, 60f)

        builder.setView(root)
            builder.setPositiveButton(
                "Yes"
            ) { _, _ ->
            }
            builder.setNegativeButton("Cancel"
            ) { dialog, _ ->
                // User cancelled the dialog
                dialog.cancel()
            }

        builder.create().show()
    }
}
