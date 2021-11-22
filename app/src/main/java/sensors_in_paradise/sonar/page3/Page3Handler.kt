package sensors_in_paradise.sonar.page3

import android.app.Activity
import android.content.Context
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
// import org.tensorflow.lite.DataType
// import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
// import sensors_in_paradise.sonar.ml.TestModel
// import java.io.File
// import java.nio.ByteBuffer

class Page3Handler : PageInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private val predictions = ArrayList<Prediction>()
    private lateinit var predictButton: Button
    override fun activityCreated(activity: Activity) {
        this.activity = activity
        this.context = activity
        recyclerView = activity.findViewById(R.id.rv_prediction)
        val prediction1 = Prediction("Squats", "90%")
        val prediction2 = Prediction("Running", "75%")

        predictions.add(prediction1)
        predictions.add(prediction2)
        adapter = PredictionsAdapter(predictions)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        predictButton = activity.findViewById(R.id.button_start_predict)
        predictButton.setOnClickListener {
            /*
            // get data and model
            var data = Page3Handler::class.java.getResource("/standing_array.raw").readBytes()
            val model = TestModel.newInstance(context)
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 128, 9), DataType.FLOAT32)
            inputFeature0.loadBuffer(ByteBuffer.wrap(data))
            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            // Releases model resources if no longer used.
            model.close()
            //this is our Output
            val test = outputFeature0.floatArray
            */
        }
    }

    override fun activityResumed() {
    }
}
