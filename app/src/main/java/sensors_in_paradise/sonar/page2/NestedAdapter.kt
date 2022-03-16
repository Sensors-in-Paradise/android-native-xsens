package sensors_in_paradise.sonar.page2

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// TODO: Check why package is needed here

class NestedAdapter(private val entries: List<String>) :
    RecyclerView.Adapter<NestedAdapter.NestedViewHolder>() {
    private var onItemClicked: ((value: String) -> Unit)? = null

    inner class NestedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val entry: TextView = view.findViewById(sensors_in_paradise.sonar.R.id.nestedItemTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(
                sensors_in_paradise.sonar.R.layout.nested_item, parent, false)
        return NestedViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: NestedViewHolder, position: Int) {
        val entryText = entries[position]
        viewHolder.entry.text = entryText
        viewHolder.entry.setOnClickListener{
            onItemClicked?.let { it1 -> it1(entryText) }
        }
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    fun setOnItemClickedListener(onItemClicked: (value: String) -> Unit) {
        this.onItemClicked = onItemClicked
    }
}