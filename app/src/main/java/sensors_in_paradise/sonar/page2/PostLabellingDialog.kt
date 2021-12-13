package sensors_in_paradise.sonar.page2

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import sensors_in_paradise.sonar.R

class PostLabellingDialog(
    context: Context,
    availableLabels: Array<String>
) {

    private var listener: (value: String) -> Unit = {_->}
    private var dialog: AlertDialog
    private var label: String? = null
    private val radioButtons = arrayListOf<RadioButton>()
    interface PostLabellingInterface {
        fun onLabelSelected(label: String)

    }

    init {

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.post_labelling_dialog, null)
        val radioGroup = root.findViewById<RadioGroup>(R.id.radioGroup_labels_postLabellingDialog)

        for(label in availableLabels){
            val rb = RadioButton(context)
            rb.setText(label)
            radioButtons.add(rb)
            radioGroup.addView(rb)
        }

        builder.setView(root)



        dialog = builder.create()

        dialog.setButton(Dialog.BUTTON_POSITIVE, "OK") { _, _ ->
            listener(label!!)
        }

        dialog.show()
        val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveBtn.isEnabled = false
        dialog.setCancelable(false)

        for(rb in radioButtons){
            rb.setOnCheckedChangeListener {_,_->
                this.label = label
                positiveBtn.isEnabled = true
            }
        }
    }
    fun setOnLabelSelectedListener(listener: (value: String) -> Unit){
        this.listener = listener
    }
}
