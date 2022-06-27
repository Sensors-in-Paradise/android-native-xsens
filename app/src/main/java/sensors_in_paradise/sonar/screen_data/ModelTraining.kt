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
import sensors_in_paradise.sonar.util.dialogs.SingleChoiceDialog
import kotlin.math.roundToInt

class ModelTraining(
    private val context: Context,
    private val recordingsManager: RecordingDataManager,
    val model: TFLiteModel,
    private val onTrainedModelSaveRequested: (recordingsUsedForTraining: List<Recording>) -> Unit
) {
    val progressDialog = ProgressDialog(context)
    private val uiHandler: Handler = Handler(Looper.getMainLooper())

    init {
        val peopleDurations = recordingsManager.getPeopleDurations(onlyUntrainedRecordings = true)
        SingleChoiceDialog(
            context,
            "Subjects to train on",
            peopleDurations.keys.toTypedArray(),
            onItemChosen = {
                val recordings = recordingsManager.getRecordingsBySubject(it, false)
                if (recordings.size >= 2) {
                    prepareAndExecuteTraining(recordings)
                } else {
                    MessageDialog(
                        context,
                        "There are only ${recordings.size} recordings of" +
                                " subject $it. To allow for validation after training, you need at least " +
                                "2 recordings so that one can be used for validation.",
                        "Not enough recordings"
                    )
                }
            })
    }

    private fun prepareAndExecuteTraining(recordings: List<Recording>) {
        progressDialog.show()

        progressDialog.setProgress(0, "Training process")
        progressDialog.setSubProgress(0, "Merging recording data files")
        try {
            Thread() {
                val dataFiles = RecordingDataManager.convertRecordings(recordings) {
                    uiHandler.run { progressDialog.setSubProgress(it) }
                }
                uiHandler.run { progressDialog.setProgress(30) }
                val validationDataFile = dataFiles[0]
                val validationDataSet = DataSet().apply {
                    add(validationDataFile)
                }

                uiHandler.run {
                    progressDialog.setSubProgress(
                        0,
                        "Converting validation recordings into batches"
                    )
                }
                val validationBatches = validationDataSet.convertToBatches(7, model.windowSize) {
                    uiHandler.run {
                        progressDialog.setSubProgress(it)
                    }
                }
                uiHandler.run {
                    progressDialog.setProgress(50)
                    progressDialog.setSubProgress(0, "Evaluating model before training")
                }

                val (accuracyBefore, cmBefore) = model.evaluate(validationBatches) { batch, _ ->
                    progressDialog.setSubProgress((batch * 100) / validationBatches.size)
                }

                val trainDataSet = DataSet().apply {
                    addAll(dataFiles.subList(1, dataFiles.size - 1))
                }

                uiHandler.run {
                    progressDialog.setSubProgress(0, "Converting training recordings into batches")
                }
                val trainBatches = trainDataSet.convertToBatches(20, model.windowSize) {
                    uiHandler.run {
                        progressDialog.setSubProgress(it)
                    }
                }
                uiHandler.run { progressDialog.setProgress(60) }
                model.train(trainBatches, NUM_EPOCHS) { epoch, batch, window ->
                    progressDialog.setSubProgress(
                        epoch,
                        "Training model. Epoch: $epoch/$NUM_EPOCHS, Batch: $batch/${trainBatches.size}, window: $window/$BATCH_SIZE"
                    )
                }
                uiHandler.run {
                    progressDialog.setProgress(90)
                    progressDialog.setSubProgress(0, "Evaluating model after training")
                }
                val (accuracyAfter, cmAfter) = model.evaluate(validationBatches) { batch, _ ->
                    progressDialog.setSubProgress((batch * 100) / validationBatches.size)
                }
                cmBefore.title = "Confusion Matrix before training (acc ${(accuracyBefore * 100).roundToInt()}%)"
                cmAfter.title = "Confusion Matrix after training (acc ${(accuracyAfter * 100).roundToInt()})"

                uiHandler.run {
                    ConfusionMatrixDialog(
                        context,
                        listOf(cmBefore, cmAfter),
                        positiveButtonText = "Save Model",
                        onPositiveButtonClickListener = { _, _ ->
                            onTrainedModelSaveRequested(trainDataSet as List<Recording>)
                        })
                }
            }.start()
        } catch (e: Exception) {
            progressDialog.dismiss()
            MessageDialog(
                context,
                "Training failed with exception: \n${e.message}",
                "Training failed"
            )
        }
    }

    companion object {
        const val NUM_EPOCHS = 5
        const val BATCH_SIZE = 20
    }
}
