package sensors_in_paradise.sonar.page2

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
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
        val wrapper: CardView = view.findViewById(R.id.wrapper)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersistentCategoriesAdapter.ViewHolder {
        val view = if (viewType == R.layout.category_item){
            LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false);
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.add_category_item, parent, false);
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PersistentCategoriesAdapter.ViewHolder, position: Int) {
        if (position == dataSet.size) {
            viewHolder.wrapper.setOnClickListener {
                val builder = AlertDialog.Builder(viewHolder.wrapper.context)
                val editText: EditText = EditText(viewHolder.wrapper.context);

                builder.setView(editText)
                builder.setMessage("Add new category")
                    .setCancelable(true)
                    .setPositiveButton("Submit") { dialog, id ->
                        val newCategory = editText.text.toString()
                        itemsStorage.addCategoryIfNotAdded(newCategory)
                        dataSet = itemsStorage.getItems()
                        notifyItemInserted(dataSet.size - 1)
                    }
                    .setNegativeButton("Cancel") { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
            return
        }

        val model: CategoryItem = dataSet[position]

        viewHolder.deleteButton.setOnClickListener {
            itemsStorage.removeCategory(model.itemText)
            if (position != -1) {
                dataSet = itemsStorage.getItems()
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

    override fun getItemCount() = dataSet.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == dataSet.size) R.layout.add_category_item else R.layout.category_item
    }

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