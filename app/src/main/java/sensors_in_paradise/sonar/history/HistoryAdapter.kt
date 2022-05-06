package sensors_in_paradise.sonar.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R

abstract class HistoryAdapter :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTV: TextView = view.findViewById(R.id.tv_title_trainingOccasion)
        val subTitleTV: TextView = view.findViewById(R.id.tv_subtitle_trainingOccasion)
        val upperSegmentView: View = view.findViewById(R.id.view_upperSegment_trainingOccasion)
        val lowerSegmentView: View = view.findViewById(R.id.view_lowerSegment_trainingOccasion)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    abstract fun getTitleOfItem(position: Int): String

    abstract fun getSubtitleOfItem(position: Int): String

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTV.text = getTitleOfItem(position)
        holder.apply {
            subTitleTV.text = getSubtitleOfItem(position)
            lowerSegmentView.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
            upperSegmentView.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        }
    }

    abstract override fun getItemCount(): Int

    /* Use this function instead of notifyItemInserted:
        With this adapter, multiple adjacent items need be re-rendered when a new item is added,
        so that the time beam stays correct
    */
    fun notifyItemAdded(position: Int) {
        notifyItemInserted(position)
        if (itemCount >= 2) {
            when (position) {
                0 -> {
                    notifyItemChanged(position + 1)
                }
                itemCount - 1 -> {
                    notifyItemChanged(position - 1)
                }
            }
        }
    }
}
