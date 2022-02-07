package sensors_in_paradise.sonar.page3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R

class PredictionsAdapter(
    private val predictions: ArrayList<Prediction>,
    private val highlightColor: Int
) :
    RecyclerView.Adapter<PredictionsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.tv_title_prediction)
        val detailsTextView: TextView = view.findViewById(R.id.tv_content_prediction)
        val cardView: CardView = view.findViewById(R.id.card_parent_prediction)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.prediction, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
       val prediction = predictions[position]
        viewHolder.titleTextView.text = prediction.title
        viewHolder.detailsTextView.text = prediction.percentageAsString()
        if (position == 0) {
            viewHolder.cardView.setCardBackgroundColor(highlightColor)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return predictions.size
    }
}
