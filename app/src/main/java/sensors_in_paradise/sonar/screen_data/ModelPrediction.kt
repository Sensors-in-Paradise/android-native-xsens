package sensors_in_paradise.sonar.screen_data

import android.content.Context
import android.os.Handler
import android.os.Looper
import sensors_in_paradise.sonar.machine_learning.DataSet
import sensors_in_paradise.sonar.machine_learning.TFLiteModel
import sensors_in_paradise.sonar.screen_recording.Recording
import sensors_in_paradise.sonar.screen_recording.RecordingDataManager
import sensors_in_paradise.sonar.util.dialogs.ConfusionMatrixDialog
import sensors_in_paradise.sonar.util.dialogs.MessageDialog
import sensors_in_paradise.sonar.util.dialogs.ProgressDialog
import kotlin.math.roundToInt

class ModelPrediction(
    private val context: Context,
    private val recordingsManager: RecordingDataManager,
    val model: TFLiteModel
) {

    private val uiHandler: Handler = Handler(Looper.getMainLooper())

    init {
        MessageDialog(
            context,
            "Do you want to include recordings that were used to train this model?",
            "Predict",
            "Include",
            { _, _ ->
                prepareAndExecutePrediction(recordingsManager)
            },
            "Exclude",
            { _, _ ->
                prepareAndExecutePrediction(
                    recordingsManager.filter { !it.metadataStorage.hasBeenUsedForOnDeviceTraining() })
            })
    }

    private fun prepareAndExecutePrediction(recordings: List<Recording>) {
        val progressDialog = ProgressDialog(context)
        uiHandler.run {
            progressDialog.show()
            progressDialog.setProgress(0, "Training process")
            progressDialog.setSubProgress(0, "Merging recording data files")
        }
        try {
            Thread() {
                val dataFiles = RecordingDataManager.convertRecordings(recordings) {
                    uiHandler.run { progressDialog.setSubProgress(it) }
                }
                uiHandler.run { progressDialog.setProgress(30) }

                val dataSet = DataSet().apply {
                    addAll(dataFiles)
                }

                uiHandler.run {
                    progressDialog.setSubProgress(
                        0,
                        "Converting recordings into batches"
                    )
                }
                val batches = dataSet.convertToBatches(7, model.windowSize) {
                    uiHandler.run {
                        progressDialog.setSubProgress(it)
                    }
                }
                uiHandler.run {
                    progressDialog.setProgress(80)
                    progressDialog.setSubProgress(0, "Evaluating model")
                }

                val (accuracyBefore, cm) = model.evaluate(batches) { batch, _ ->
                    progressDialog.setSubProgress((batch * 100) / batches.size)
                }

                uiHandler.run { progressDialog.setProgress(100) }

                cm.title =
                    "Confusion Matrix (acc ${(accuracyBefore * 100).roundToInt()}%)"
                uiHandler.run {
                    progressDialog.setProgress(100)
                    progressDialog.dismiss()
                }
                uiHandler.run {
                    ConfusionMatrixDialog(
                        context,
                        listOf(cm)
                    )
                }
            }.start()
        } catch (e: Exception) {
            progressDialog.dismiss()
            MessageDialog(
                context,
                "Prediction failed with exception: \n${e.message}",
                "Prediction failed"
            )
        }
    }
}
