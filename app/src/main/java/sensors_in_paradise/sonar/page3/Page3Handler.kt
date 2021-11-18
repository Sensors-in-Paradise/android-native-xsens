package sensors_in_paradise.sonar.page3

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R

class Page3Handler:PageInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private val predictions = ArrayList<Prediction>()
    override fun activityCreated(activity: Activity) {
        this.activity = activity
        this.context = activity
        recyclerView = activity.findViewById(R.id.rv_prediction)
        val prediction1  = Prediction("Squats", "90%")
        val prediction2  = Prediction("Running", "75%")

        predictions.add(prediction1)
        predictions.add(prediction2)
        adapter = PredictionsAdapter(predictions)
        recyclerView.adapter=adapter
        Log.d("ADAPTER", "222SIZE PREDICTION: "+adapter.itemCount)
        Toast.makeText(context, "Moin", Toast.LENGTH_LONG).show()
        adapter.notifyDataSetChanged()
    }

    override fun activityResumed() {

    }
}
