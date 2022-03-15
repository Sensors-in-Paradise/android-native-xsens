package sensors_in_paradise.sonar.page2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R

class PersistentCategoriesAdapter(private val itemsStorage: StringItemStorage) :
    RecyclerView.Adapter<PersistentCategoriesAdapter.ViewHolder>() {
    private var dataSet = itemsStorage.items
    private var filterText: String = ""

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val linearLayout: LinearLayout = view.findViewById(R.id.linear_layout)
        val expandableLayout: RelativeLayout = view.findViewById(R.id.expandable_layout)
        val mTextView: TextView = view.findViewById(R.id.itemTv)
        val mArrowImage: ImageView = view.findViewById(R.id.arrow_imageview)
        val nestedRecyclerView: RecyclerView = view.findViewById(R.id.child_rv)

        val deleteButton: ImageButton = view.findViewById(R.id.btn_delete_stringsDialogItem)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PersistentCategoriesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)

        return PersistentCategoriesAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PersistentCategoriesAdapter.ViewHolder, position: Int) {
        val item = getFilteredItemAt(position)
        if (item != null) {
            viewHolder.deleteButton.setOnClickListener {
                val index = getFilteredItemIndex(item)
                itemsStorage.removeItem(item)
                if (index != -1) {
                    notifyItemRemoved(index)
                }
            }
            val isDeletable = !itemsStorage.nonDeletableItems.contains(item)
            viewHolder.deleteButton.visibility = if (isDeletable) View.VISIBLE else View.INVISIBLE

            viewHolder.mTextView.text = item.getItemText()

            val isExpandable: Boolean = item.isExpandable()
            viewHolder.expandableLayout.setVisibility(if (isExpandable) View.VISIBLE else View.GONE)

            viewHolder.label.text = item
            viewHolder.label.setOnClickListener {
                onItemClicked?.let { it1 -> it1(item) }
            }
        } else {
            viewHolder.itemView.setBackgroundColor(Color.RED)
        }
    }

//    fun setOnItemClickedListener(onItemClicked: (value: String) -> Unit) {
//        this.onItemClicked = onItemClicked
//    }

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

    private fun getFilteredItemIndex(item: String): Int {
        var count = 0

        for (i in 0 until dataSet.length()) {
            val currentItem = dataSet[i].toString()
            if (isItemMatchedByFilter(item)) {
                if (currentItem == item) {
                    return count
                }
                count++
            }
        }
        return -1
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