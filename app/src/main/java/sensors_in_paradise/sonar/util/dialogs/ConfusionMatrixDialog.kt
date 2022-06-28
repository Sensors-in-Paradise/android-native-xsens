package sensors_in_paradise.sonar.util.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ViewAnimator
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.custom_views.confusion_matrix.ConfusionMatrix
import sensors_in_paradise.sonar.custom_views.confusion_matrix.ConfusionMatrixView

class ConfusionMatrixDialog(
    context: Context,
    private val confusionMatrices: List<ConfusionMatrix>,
    positiveButtonText: String = "Okay",
    onPositiveButtonClickListener: DialogInterface.OnClickListener? = null,
    neutralButtonText: String = "Neutral",
    onNeutralButtonClickListener: DialogInterface.OnClickListener? = null
) {
    private val dialog: AlertDialog
    private var index: Int = 0
    private val nextBtn: ImageButton
    private val previousBtn: ImageButton
    private val positionTv: TextView
    private val navigationFrameLayout: FrameLayout

    init {
        val builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.confusion_matrix_dialog, null)
        val viewAnimator = root.findViewById<ViewAnimator>(R.id.viewAnimator_confusionMatrices_confusionMatrixDialog)

        for (cm in confusionMatrices) {
            val confusionMatrixView = ConfusionMatrixView(context)
            confusionMatrixView.setConfusionMatrix(cm)
            confusionMatrixView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            viewAnimator.addView(confusionMatrixView)
        }
        nextBtn = root.findViewById(R.id.button_next_confusionMatrixDialog)
        previousBtn = root.findViewById(R.id.button_previous_confusionMatrixDialog)
        positionTv = root.findViewById(R.id.textView_navigation_confusionMatrixDialog)
        navigationFrameLayout = root.findViewById(R.id.frameLayout_navigation_confusionMatrixDialog)

        nextBtn.setOnClickListener {
            viewAnimator.displayedChild = ++index
            updateNavigationUi()
        }
        previousBtn.setOnClickListener {
            viewAnimator.displayedChild = --index
            updateNavigationUi()
        }

        builder.setView(root)

        if (onNeutralButtonClickListener != null) {
            builder.setNeutralButton(neutralButtonText, onNeutralButtonClickListener)
        }

        builder.setPositiveButton(
            positiveButtonText, null
        )
        if (onPositiveButtonClickListener != null) {
            builder.setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                // User cancelled the dialog
                dialog.cancel()
            }
        }
        builder.setOnDismissListener {
        }
        // Create the AlertDialog object and return it
        dialog = builder.create()
        updateNavigationUi()
        dialog.show()
    }
    private fun updateNavigationUi() {
        previousBtn.isEnabled = index > 0
        nextBtn.isEnabled = index < confusionMatrices.size - 1
        dialog.setTitle(confusionMatrices[index].title)
        positionTv.text = "${index+1}/${confusionMatrices.size}"
        navigationFrameLayout.visibility = if(confusionMatrices.size>1) View.VISIBLE else View.GONE
    }
}
