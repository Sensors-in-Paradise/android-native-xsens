package sensors_in_paradise.sonar.page2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R


class PersistentCategoriesAdapter(private val itemsStorage: CategoryItemStorage) :
    RecyclerView.Adapter<PersistentCategoriesAdapter.ViewHolder>() {
    private val dataSet: List<CategoryItem> = itemsStorage.getItems()
    private var nestedItems: List<String> = ArrayList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val linearLayout: LinearLayout = view.findViewById(R.id.linear_layout)
        val expandableLayout: RelativeLayout = view.findViewById(R.id.expandable_layout)
        val mTextView: TextView = view.findViewById(R.id.itemTv)
        val mArrowImage: ImageView = view.findViewById(R.id.arrow_imageview)
        val nestedRecyclerView: RecyclerView = view.findViewById(R.id.child_rv)

        val deleteButton: ImageButton = view.findViewById(R.id.btn_delete_stringsDialogItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersistentCategoriesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)

        return PersistentCategoriesAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: PersistentCategoriesAdapter.ViewHolder, position: Int) {
        val model: CategoryItem = dataSet[position]

        viewHolder.deleteButton.setOnClickListener {
            itemsStorage.removeCategory(model.itemText)
            if (position != -1) {
                notifyItemRemoved(position)
            }
        }

        val isDeletable = !itemsStorage.nonDeletableItems.contains(model.itemText)
        viewHolder.deleteButton.visibility = if (isDeletable) View.VISIBLE else View.INVISIBLE

        viewHolder.mTextView.text = model.itemText

        val isExpanded: Boolean = model.isExpanded
        viewHolder.expandableLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE

        // TODO: Change images
        if (isExpanded) {
            viewHolder.mArrowImage.setImageResource(R.drawable.ic_baseline_delete_forever_24);
        } else{
            viewHolder.mArrowImage.setImageResource(R.drawable.ic_baseline_add_circle_outline_24);
        }

        val nestedAdapter = NestedAdapter(nestedItems)
        viewHolder.nestedRecyclerView.layoutManager = GridLayoutManager(viewHolder.itemView.getContext(), 2)
        viewHolder.nestedRecyclerView.setHasFixedSize(true)
        viewHolder.nestedRecyclerView.adapter = nestedAdapter
        viewHolder.mArrowImage.setOnClickListener(View.OnClickListener {
            model.isExpanded = !model.isExpanded
            nestedItems = model.nestedList
            notifyItemChanged(viewHolder.adapterPosition)
        })
    }

    override fun getItemCount() = dataSet.size
}