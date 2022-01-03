package sensors_in_paradise.sonar.page2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R

class PersistentStringArrayAdapter(private val itemsStorage: StringItemStorage) :
    RecyclerView.Adapter<PersistentStringArrayAdapter.ViewHolder>() {
    private var dataSet = itemsStorage.items
    private var filterText: String = ""

    private var onItemClicked: ((value: String) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView
        val deleteButton: ImageButton

        init {
            label = view.findViewById(R.id.tv_stringsDialogItem)
            deleteButton = view.findViewById(R.id.btn_delete_stringsDialogItem)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.strings_dialog_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getFilteredItemAt(position)
        if (item != null) {
            viewHolder.deleteButton.setOnClickListener {
                itemsStorage.removeItem(item)
                notifyItemRemoved(position)
            }
            viewHolder.label.text = item
            viewHolder.label.setOnClickListener {
                onItemClicked?.let { it1 -> it1(item) }
            }

        } else {
            viewHolder.itemView.setBackgroundColor(Color.RED)
        }
    }

    fun setOnItemClickedListener(onItemClicked: (value: String) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    override fun getItemCount() = getFilteredItemCount()
    private fun isItemMatchedByFilter(item: String): Boolean {
        return item.contains(filterText)
    }

    private fun getFilteredItemCount(): Int {
        var count = 0
        for (i in 0 until dataSet.length()) {
            val item = dataSet[i].toString()
            if (isItemMatchedByFilter(item)) {
                count++
            }
        }
        return count
    }

    private fun getFilteredItemAt(index: Int): String? {
        var count = 0
        if (index < 0 || index >= getFilteredItemCount()) {
            return null
        }
        for (i in 0 until dataSet.length()) {
            val item = dataSet[i].toString()
            if (isItemMatchedByFilter(item)) {
                if (count == index) {
                    return item
                }
                count++
            }
        }
        return null
    }

    fun filter(text: String) {
        filterText = text
        notifyDataSetChanged()
    }
}
