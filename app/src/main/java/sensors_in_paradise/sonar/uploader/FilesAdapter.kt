package sensors_in_paradise.sonar.uploader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewAnimator
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R
import java.io.File

class FilesAdapter(
    private val fileItems: FileUIItemArrayList,
) :
    RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelTV: TextView = view.findViewById(R.id.tv_label_uploadFile)
        val statusTV: TextView = view.findViewById(R.id.tv_status_uploadFile)
        val animator: ViewAnimator = view.findViewById(R.id.switcher_uploadFile)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.upload_file, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
       val fileItem = fileItems[position]
        viewHolder.labelTV.text = fileItem.label
        viewHolder.statusTV.text = fileItem.statusLabel()
        viewHolder.animator.displayedChild = fileItem.status.ordinal
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return fileItems.size
    }
    fun notifyItemChanged(file: File) {
        val index = fileItems.indexOf(file)
        if (index != -1) {
            notifyItemChanged(index)
        }
    }
}
