package sensors_in_paradise.sonar.page2

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class NestedAdapter(private val mList: List<String>) :
    RecyclerView.Adapter<NestedAdapter.NestedViewHolder>() {

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): NestedViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.nested_item, parent, false)
        return NestedViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: NestedViewHolder, position: Int) {
        holder.mTv.text = mList[position]
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class NestedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mTv: TextView = view.findViewById(R.id.nestedItemTv)
    }
}