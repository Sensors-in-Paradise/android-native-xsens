package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import sensors_in_paradise.sonar.R

class RecordingLabelsAdapter(activity: Activity) :
    ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item) {
    val selectIcon = context.getDrawable(R.drawable.ic_baseline_error_outline_24)
    val addIcon = context.getDrawable(R.drawable.ic_baseline_add_circle_outline_24)
    val labelIcon = context.getDrawable(R.drawable.ic_baseline_label_24)

    private var clickInterface: ClickInterface? = null

    interface ClickInterface {
        fun onDeleteButtonPressed(label: String)
    }

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView =
                LayoutInflater.from(context).inflate(R.layout.labels_spinner_item, parent, false)
        }
        val isLabel = pos != 0 && pos != count - 1
        val tv = itemView!!.findViewById<TextView>(R.id.tv_labelsSpinnerItem)
        val delBtn = itemView.findViewById<ImageButton>(R.id.btn_delete_spinnerItem)
        val img = itemView.findViewById<ImageView>(R.id.iv_labelsSpinnerItem)
        delBtn.visibility = if (isLabel) View.VISIBLE else View.INVISIBLE
        delBtn.setOnClickListener {
            clickInterface?.onDeleteButtonPressed(getItem(pos)!!)
        }
        val drawable: Drawable? = when (pos) {
            0 -> selectIcon
            count - 1 -> addIcon
            else -> {
                labelIcon
            }
        }
        img.setImageDrawable(drawable)
        tv.text = getItem(pos)
        return itemView
    }

    fun setDeleteButtonClickListener(clickInterface: ClickInterface) {
        this.clickInterface = clickInterface
    }
}
