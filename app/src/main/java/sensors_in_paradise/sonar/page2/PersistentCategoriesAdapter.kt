package sensors_in_paradise.sonar.page2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R


class PersistentCategoriesAdapter(private val itemsStorage: CategoryItemStorage) :
    RecyclerView.Adapter<PersistentCategoriesAdapter.ViewHolder>() {
    private var dataSet: List<CategoryItem> = itemsStorage.getItems()
    private var nestedItems: List<String> = ArrayList()

    // This is handed over to the nested recyclerviews
    private var onItemClicked: ((value: String) -> Unit)? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val linearLayout: LinearLayout = view.findViewById(R.id.linear_layout)
        val expandableLayout: RelativeLayout = view.findViewById(R.id.expandable_layout)
        val mTextView: TextView = view.findViewById(R.id.itemTv)
        val mArrowImage: ImageView = view.findViewById(R.id.arrow_imageview)
        val nestedRecyclerView: RecyclerView = view.findViewById(R.id.child_rv)
        val deleteButton: ImageView = view.findViewById(R.id.btn_delete_stringsDialogItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersistentCategoriesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PersistentCategoriesAdapter.ViewHolder, position: Int) {
        val model: CategoryItem = dataSet[position]

        viewHolder.deleteButton.setOnClickListener {
            itemsStorage.removeCategory(model.itemText)
            if (position != -1) {
                notifyItemRemoved(position)
            }
        }

        val isDeletable = !itemsStorage.nonDeletableCategories.contains(model.itemText)
        viewHolder.deleteButton.visibility = if (isDeletable) View.VISIBLE else View.INVISIBLE

        viewHolder.mTextView.text = model.itemText

        val isExpanded: Boolean = model.isExpanded
        viewHolder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

        if (isExpanded) {
            viewHolder.mArrowImage.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        } else {
            viewHolder.mArrowImage.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        }

        val nestedAdapter = NestedAdapter(nestedItems)
        viewHolder.nestedRecyclerView.layoutManager = GridLayoutManager(viewHolder.itemView.getContext(), 2)
        viewHolder.nestedRecyclerView.setHasFixedSize(true)
        viewHolder.nestedRecyclerView.adapter = nestedAdapter

        nestedAdapter.setOnItemClickedListener { value -> this.onItemClicked?.let { it(value) } }

        nestedAdapter.setOnItemLongClickedListener { value:String, index:Int ->
            itemsStorage.removeEntry(value)
            if (index != -1) {
                update(position)
            }
        }

        viewHolder.mArrowImage.setOnClickListener {
            model.isExpanded = !model.isExpanded
            nestedItems = model.nestedList
            notifyItemChanged(viewHolder.adapterPosition)
        }
    }

    override fun getItemCount() = dataSet.size

    fun setOnItemClickedListener(onItemClicked: (value: String) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    fun updateByCategory(category: String) {
        val index = findIndexByCategory(category)
        if (index != -1) update(index)
    }

    private fun findIndexByCategory(category: String): Int {
        for (i in dataSet.indices) {
            if (dataSet[i].itemText == category) {
                return i
            }
        }
        return -1
    }

    private fun update(position: Int) {
        dataSet = itemsStorage.getItems()
        dataSet[position].isExpanded = true
        nestedItems = dataSet[position].nestedList
        notifyItemChanged(position)
    }
}