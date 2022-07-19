package sensors_in_paradise.sonar

import android.app.Activity
import android.os.Environment.DIRECTORY_PICTURES
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.screenshot.Screenshot
import org.junit.Test
import org.junit.runner.RunWith
import sensors_in_paradise.sonar.custom_views.confusion_matrix.ConfusionMatrix
import sensors_in_paradise.sonar.machine_learning.DataSet
import sensors_in_paradise.sonar.machine_learning.TFLiteModel
import sensors_in_paradise.sonar.screen_recording.RecordingDataFile
import sensors_in_paradise.sonar.use_cases.UseCase
import sensors_in_paradise.sonar.util.dialogs.ConfusionMatrixDialog
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class GaitExperimentsTest {
    private val assetContext = InstrumentationRegistry.getInstrumentation().context
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val recordingFileSub01STFatigue =
        File(appContext.cacheDir, "recordingData01STFatigue.csv")
    private val recordingFileSub01STNonFatigue =
        File(appContext.cacheDir, "recordingData01STNonFatigue.csv")
    private val recordingFileSub01DTFatigue =
        File(appContext.cacheDir, "recordingData01DTFatigue.csv")
    private val recordingFileSub01DTNonFatigue =
        File(appContext.cacheDir, "recordingData01DTNonFatigue.csv")
    private val recordingFileSub02STFatigue =
        File(appContext.cacheDir, "recordingData02STFatigue.csv")
    private val recordingFileSub02STNonFatigue =
        File(appContext.cacheDir, "recordingData02STNonFatigue.csv")
    private val recordingFileSub02DTFatigue =
        File(appContext.cacheDir, "recordingData02DTFatigue.csv")
    private val recordingFileSub02DTNonFatigue =
        File(appContext.cacheDir, "recordingData02DTNonFatigue.csv")
    private val recordingFileSub03STFatigue =
        File(appContext.cacheDir, "recordingData03STFatigue.csv")
    private val recordingFileSub03STNonFatigue =
        File(appContext.cacheDir, "recordingData03STNonFatigue.csv")
    private val recordingFileSub03DTFatigue =
        File(appContext.cacheDir, "recordingData03DTFatigue.csv")
    private val recordingFileSub03DTNonFatigue =
        File(appContext.cacheDir, "recordingData03DTNonFatigue.csv")
    private val recordingFileSub05STFatigue =
        File(appContext.cacheDir, "recordingData05STFatigue.csv")
    private val recordingFileSub05STNonFatigue =
        File(appContext.cacheDir, "recordingData05STNonFatigue.csv")
    private val recordingFileSub05DTFatigue =
        File(appContext.cacheDir, "recordingData05DTFatigue.csv")
    private val recordingFileSub05DTNonFatigue =
        File(appContext.cacheDir, "recordingData05DTNonFatigue.csv")
    private val recordingFileSub06STFatigue =
        File(appContext.cacheDir, "recordingData06STFatigue.csv")
    private val recordingFileSub06STNonFatigue =
        File(appContext.cacheDir, "recordingData06STNonFatigue.csv")
    private val recordingFileSub06DTFatigue =
        File(appContext.cacheDir, "recordingData06DTFatigue.csv")
    private val recordingFileSub06DTNonFatigue =
        File(appContext.cacheDir, "recordingData06DTNonFatigue.csv")
    private val recordingFileSub07STFatigue =
        File(appContext.cacheDir, "recordingData07STFatigue.csv")
    private val recordingFileSub07STNonFatigue =
        File(appContext.cacheDir, "recordingData07STNonFatigue.csv")
    private val recordingFileSub07DTFatigue =
        File(appContext.cacheDir, "recordingData07DTFatigue.csv")
    private val recordingFileSub07DTNonFatigue =
        File(appContext.cacheDir, "recordingData07DTNonFatigue.csv")
    private val recordingFileSub08STFatigue =
        File(appContext.cacheDir, "recordingData08STFatigue.csv")
    private val recordingFileSub08STNonFatigue =
        File(appContext.cacheDir, "recordingData08STNonFatigue.csv")
    private val recordingFileSub08DTFatigue =
        File(appContext.cacheDir, "recordingData08DTFatigue.csv")
    private val recordingFileSub08DTNonFatigue =
        File(appContext.cacheDir, "recordingData08DTNonFatigue.csv")
    private val recordingFileSub09STFatigue =
        File(appContext.cacheDir, "recordingData09STFatigue.csv")
    private val recordingFileSub09STNonFatigue =
        File(appContext.cacheDir, "recordingData09STNonFatigue.csv")
    private val recordingFileSub09DTFatigue =
        File(appContext.cacheDir, "recordingData09DTFatigue.csv")
    private val recordingFileSub09DTNonFatigue =
        File(appContext.cacheDir, "recordingData09DTNonFatigue.csv")
    private val recordingFileSub10STFatigue =
        File(appContext.cacheDir, "recordingData10STFatigue.csv")
    private val recordingFileSub10STNonFatigue =
        File(appContext.cacheDir, "recordingData10STNonFatigue.csv")
    private val recordingFileSub10DTFatigue =
        File(appContext.cacheDir, "recordingData10DTFatigue.csv")
    private val recordingFileSub10DTNonFatigue =
        File(appContext.cacheDir, "recordingData10DTNonFatigue.csv")
    private val recordingFileSub11STFatigue =
        File(appContext.cacheDir, "recordingData11STFatigue.csv")
    private val recordingFileSub11STNonFatigue =
        File(appContext.cacheDir, "recordingData11STNonFatigue.csv")
    private val recordingFileSub11DTFatigue =
        File(appContext.cacheDir, "recordingData11DTFatigue.csv")
    private val recordingFileSub11DTNonFatigue =
        File(appContext.cacheDir, "recordingData11DTNonFatigue.csv")
    private val recordingFileSub12STFatigue =
        File(appContext.cacheDir, "recordingData12STFatigue.csv")
    private val recordingFileSub12STNonFatigue =
        File(appContext.cacheDir, "recordingData12STNonFatigue.csv")
    private val recordingFileSub12DTFatigue =
        File(appContext.cacheDir, "recordingData12DTFatigue.csv")
    private val recordingFileSub12DTNonFatigue =
        File(appContext.cacheDir, "recordingData12DTNonFatigue.csv")
    private val recordingFileSub13STFatigue =
        File(appContext.cacheDir, "recordingData13STFatigue.csv")
    private val recordingFileSub13STNonFatigue =
        File(appContext.cacheDir, "recordingData13STNonFatigue.csv")
    private val recordingFileSub13DTFatigue =
        File(appContext.cacheDir, "recordingData13DTFatigue.csv")
    private val recordingFileSub13DTNonFatigue =
        File(appContext.cacheDir, "recordingData13DTNonFatigue.csv")
    private val recordingFileSub14STFatigue =
        File(appContext.cacheDir, "recordingData14STFatigue.csv")
    private val recordingFileSub14STNonFatigue =
        File(appContext.cacheDir, "recordingData14STNonFatigue.csv")
    private val recordingFileSub14DTFatigue =
        File(appContext.cacheDir, "recordingData14DTFatigue.csv")
    private val recordingFileSub14DTNonFatigue =
        File(appContext.cacheDir, "recordingData14DTNonFatigue.csv")
    private val recordingFileSub15STFatigue =
        File(appContext.cacheDir, "recordingData15STFatigue.csv")
    private val recordingFileSub15STNonFatigue =
        File(appContext.cacheDir, "recordingData15STNonFatigue.csv")
    private val recordingFileSub15DTFatigue =
        File(appContext.cacheDir, "recordingData15DTFatigue.csv")
    private val recordingFileSub15DTNonFatigue =
        File(appContext.cacheDir, "recordingData15DTNonFatigue.csv")
    private val recordingFileSub17STFatigue =
        File(appContext.cacheDir, "recordingData17STFatigue.csv")
    private val recordingFileSub17STNonFatigue =
        File(appContext.cacheDir, "recordingData17STNonFatigue.csv")
    private val recordingFileSub17DTFatigue =
        File(appContext.cacheDir, "recordingData17DTFatigue.csv")
    private val recordingFileSub17DTNonFatigue =
        File(appContext.cacheDir, "recordingData17DTNonFatigue.csv")
    private val recordingFileSub18STFatigue =
        File(appContext.cacheDir, "recordingData18STFatigue.csv")
    private val recordingFileSub18STNonFatigue =
        File(appContext.cacheDir, "recordingData18STNonFatigue.csv")
    private val recordingFileSub18DTFatigue =
        File(appContext.cacheDir, "recordingData18DTFatigue.csv")
    private val recordingFileSub18DTNonFatigue =
        File(appContext.cacheDir, "recordingData18DTNonFatigue.csv")


    private val modelFileSub01 = File(appContext.cacheDir, "model_test01.tflite")
    private val modelFileSub02 = File(appContext.cacheDir, "model_test02.tflite")
    private val modelFileSub03 = File(appContext.cacheDir, "model_test03.tflite")
    private val modelFileSub05 = File(appContext.cacheDir, "model_test05.tflite")
    private val modelFileSub06 = File(appContext.cacheDir, "model_test06.tflite")
    private val modelFileSub07 = File(appContext.cacheDir, "model_test07.tflite")
    private val modelFileSub08 = File(appContext.cacheDir, "model_test08.tflite")
    private val modelFileSub09 = File(appContext.cacheDir, "model_test09.tflite")
    private val modelFileSub10 = File(appContext.cacheDir, "model_test10.tflite")
    private val modelFileSub11 = File(appContext.cacheDir, "model_test11.tflite")
    private val modelFileSub12 = File(appContext.cacheDir, "model_test12.tflite")
    private val modelFileSub13 = File(appContext.cacheDir, "model_test13.tflite")
    private val modelFileSub14 = File(appContext.cacheDir, "model_test14.tflite")
    private val modelFileSub15 = File(appContext.cacheDir, "model_test15.tflite")
    private val modelFileSub17 = File(appContext.cacheDir, "model_test17.tflite")
    private val modelFileSub18 = File(appContext.cacheDir, "model_test18.tflite")

    @Test
    fun trainAllSubsTest() {
        subs.forEach { sub ->
            trainSub(sub)
        }
    }

    fun trainSub(sub: String) {
        extractFilesForSub(sub)
        val model = TFLiteModel(modelFileSub(sub))

        val features = model.getFeaturesToPredict().map { it.uppercase() }.toTypedArray()

        val stFatigueData = RecordingDataFile(recordingFileSTFatigue(sub))
        val stNonFatigueData = RecordingDataFile(recordingFileSTNonFatigue(sub))
        val dtFatigueData = RecordingDataFile(recordingFileDTFatigue(sub))
        val dtNonFatigueData = RecordingDataFile(recordingFileDTNonFatigue(sub))

        val dataSet = DataSet().apply {
            add(stFatigueData)
            add(stNonFatigueData)
            add(dtFatigueData)
            add(dtNonFatigueData)
        }
        runAndEvalTraining(dataSet, model, features, sub)
    }

    fun runAndEvalTraining(
        dataSet: DataSet,
        model: TFLiteModel,
        features: Array<String>,
        sub: String
    ) {
        val (trainingResults, batchSizes) = runTraining(dataSet, model, features)
        trainingResults.confusionMatrixBeforeTraining.description =
            "Accuracy before training: ${(trainingResults.accuracyBeforeTraining * 100).toInt()}%"
        trainingResults.confusionMatrixAfterTraining.description =
            "Accuracy after training: ${(trainingResults.accuracyAfterTraining * 100).toInt()}%." +
                    "\nValidation data comprised of ${batchSizes.second} batches with 7 windows per batch." +
                    "\nTraining data comprised of ${batchSizes.first} batches with 7 windows per batch."
        screenshotMatrices(
            listOf(
                trainingResults.confusionMatrixBeforeTraining,
                trainingResults.confusionMatrixAfterTraining
            ),
            "${sub}before",
            "${sub}after"
        )
    }

    fun extractFilesForSub(sub: String) {
        val recordingFileSubSTFatigue = when (sub) {
            "01" -> recordingFileSub01STFatigue
            "02" -> recordingFileSub02STFatigue
            "03" -> recordingFileSub03STFatigue
            "05" -> recordingFileSub05STFatigue
            "06" -> recordingFileSub06STFatigue
            "07" -> recordingFileSub07STFatigue
            "08" -> recordingFileSub08STFatigue
            "09" -> recordingFileSub09STFatigue
            "10" -> recordingFileSub10STFatigue
            "11" -> recordingFileSub11STFatigue
            "12" -> recordingFileSub12STFatigue
            "13" -> recordingFileSub13STFatigue
            "14" -> recordingFileSub14STFatigue
            "15" -> recordingFileSub15STFatigue
            "17" -> recordingFileSub17STFatigue
            "18" -> recordingFileSub18STFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }
        UseCase.extractFileFromAssets(
            assetContext,
            "OG_st_fatigue_sub_${sub}.csv",
            recordingFileSubSTFatigue
        )
        val recordingFileSubSTNonFatigue = when (sub) {
            "01" -> recordingFileSub01STNonFatigue
            "02" -> recordingFileSub02STNonFatigue
            "03" -> recordingFileSub03STNonFatigue
            "05" -> recordingFileSub05STNonFatigue
            "06" -> recordingFileSub06STNonFatigue
            "07" -> recordingFileSub07STNonFatigue
            "08" -> recordingFileSub08STNonFatigue
            "09" -> recordingFileSub09STNonFatigue
            "10" -> recordingFileSub10STNonFatigue
            "11" -> recordingFileSub11STNonFatigue
            "12" -> recordingFileSub12STNonFatigue
            "13" -> recordingFileSub13STNonFatigue
            "14" -> recordingFileSub14STNonFatigue
            "15" -> recordingFileSub15STNonFatigue
            "17" -> recordingFileSub17STNonFatigue
            "18" -> recordingFileSub18STNonFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }

        UseCase.extractFileFromAssets(
            assetContext,
            "OG_st_control_sub_${sub}.csv",
            recordingFileSubSTNonFatigue
        )

        val recordingFileSubDTFatigue = when (sub) {
            "01" -> recordingFileSub01DTFatigue
            "02" -> recordingFileSub02DTFatigue
            "03" -> recordingFileSub03DTFatigue
            "05" -> recordingFileSub05DTFatigue
            "06" -> recordingFileSub06DTFatigue
            "07" -> recordingFileSub07DTFatigue
            "08" -> recordingFileSub08DTFatigue
            "09" -> recordingFileSub09DTFatigue
            "10" -> recordingFileSub10DTFatigue
            "11" -> recordingFileSub11DTFatigue
            "12" -> recordingFileSub12DTFatigue
            "13" -> recordingFileSub13DTFatigue
            "14" -> recordingFileSub14DTFatigue
            "15" -> recordingFileSub15DTFatigue
            "17" -> recordingFileSub17DTFatigue
            "18" -> recordingFileSub18DTFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }

        UseCase.extractFileFromAssets(
            assetContext,
            "OG_dt_fatigue_sub_${sub}.csv",
            recordingFileSubDTFatigue
        )

        val recordingFileSubDTNonFatigue = when (sub) {
            "01" -> recordingFileSub01DTNonFatigue
            "02" -> recordingFileSub02DTNonFatigue
            "03" -> recordingFileSub03DTNonFatigue
            "05" -> recordingFileSub05DTNonFatigue
            "06" -> recordingFileSub06DTNonFatigue
            "07" -> recordingFileSub07DTNonFatigue
            "08" -> recordingFileSub08DTNonFatigue
            "09" -> recordingFileSub09DTNonFatigue
            "10" -> recordingFileSub10DTNonFatigue
            "11" -> recordingFileSub11DTNonFatigue
            "12" -> recordingFileSub12DTNonFatigue
            "13" -> recordingFileSub13DTNonFatigue
            "14" -> recordingFileSub14DTNonFatigue
            "15" -> recordingFileSub15DTNonFatigue
            "17" -> recordingFileSub17DTNonFatigue
            "18" -> recordingFileSub18DTNonFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }

        UseCase.extractFileFromAssets(
            assetContext,
            "OG_dt_control_sub_${sub}.csv",
            recordingFileSubDTNonFatigue
        )

        val modelFile = when (sub) {
            "01" -> modelFileSub01
            "02" -> modelFileSub02
            "03" -> modelFileSub03
            "05" -> modelFileSub05
            "06" -> modelFileSub06
            "07" -> modelFileSub07
            "08" -> modelFileSub08
            "09" -> modelFileSub09
            "10" -> modelFileSub10
            "11" -> modelFileSub11
            "12" -> modelFileSub12
            "13" -> modelFileSub13
            "14" -> modelFileSub14
            "15" -> modelFileSub15
            "17" -> modelFileSub17
            "18" -> modelFileSub18
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }

        UseCase.extractFileFromAssets(assetContext, "CNNTL_model_sub_${sub}.tflite", modelFile)
    }


    fun runTraining(
        dataSet: DataSet,
        model: TFLiteModel,
        features: Array<String>
    ): Pair<TrainingResult, Pair<Int, Int>> {


        val (trainBatches, valBatches) = dataSet.convertToTrainValBatches(
            7,
            model.windowSize,
            progressCallback = { progress ->
                Log.d(
                    "RecordingDataFileTest-trainingPipelineTest",
                    "Batching dataset: $progress%"
                )
            },
            splitPercentage = 0.8f
        )

        val (accuracyBeforeTraining, confusionMatrixBeforeTraining) = model.evaluate(valBatches) { batch, window ->
            Log.d(
                "RecordingDataFileTest",
                "Evaluating model before training. Batch: $batch%, Window Progress: $window%"
            )
        }

        val losses = model.train(trainBatches, 5) { epoch, batch, window ->
            Log.d(
                "RecordingDataFileTest",
                "Training model. Epoch $epoch%, batch $batch%, window $window%"
            )
        }

        Log.d(
            "RecordingDataFileTest",
            "Losses during training epochs: ${losses.joinToString()}"
        )

        val (accuracyAfterTraining, confusionMatrixAfterTraining) = model.evaluate(valBatches) { batch, window ->
            Log.d(
                "RecordingDataFileTest",
                "Evaluating model after training. Batch: $batch%, Window Progress: $window%"
            )
        }

        Log.d(
            "RecordingDataFileTest",
            "Prediction accuracy on the example recording before training: " +
                    "$accuracyBeforeTraining and after training: $accuracyAfterTraining%"
        )
        return Pair(
            TrainingResult(
                accuracyBeforeTraining,
                accuracyAfterTraining,
                confusionMatrixBeforeTraining,
                confusionMatrixAfterTraining
            ),
            Pair(trainBatches.size, valBatches.size)
        )
    }

    fun screenshotMatrices(matrices: List<ConfusionMatrix>, name1: String, name2: String) {
        launchActivity<MainActivity>().use { scenario ->

            scenario.onActivity { activity ->
                screenshotMatrices(activity, matrices, name1, name2)

                val dir =
                    android.os.Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)

                Log.d("GaitExperimentsTest", "$dir")
            }
            scenario.moveToState(Lifecycle.State.CREATED)
        }
    }

    private val TAG = "ScreenshotsUtils"
    fun screenshotMatrix(
        activity: Activity,
        matrices: List<ConfusionMatrix>,
        matrixIndex: Int,
        name: String,
        onDone: () -> Unit
    ) {
        val dialog = ConfusionMatrixDialog(activity, matrices, showAutomatically = false)
        dialog.setOnShowListener {
            Thread.sleep(1000)
            takeScreenshot(name)
            dialog.dismiss()

            onDone()
        }
        dialog.setDisplayConfusionMatrix(matrixIndex)
        dialog.show()
    }

    fun screenshotMatrices(
        activity: Activity,
        matrices: List<ConfusionMatrix>,
        name1: String,
        name2: String
    ) {
        screenshotMatrix(activity, matrices, 0, name1) {
            screenshotMatrix(activity, matrices, 1, name2) {
            }
        }
    }

    fun takeScreenshot(screenShotName: String) {
        Log.d(TAG, "Taking screenshot of '$screenShotName'")

        val screenCapture = Screenshot.capture()
        try {
            screenCapture.apply {
                name = screenShotName
                process()
            }
            Log.d(TAG, "Screenshot taken")
        } catch (ex: IOException) {
            Log.e(TAG, "Could not take a screenshot", ex)
        }
    }

    fun modelFileSub(sub: String): File {
        return when (sub) {
            "01" -> modelFileSub01
            "02" -> modelFileSub02
            "03" -> modelFileSub03
            "05" -> modelFileSub05
            "06" -> modelFileSub06
            "07" -> modelFileSub07
            "08" -> modelFileSub08
            "09" -> modelFileSub09
            "10" -> modelFileSub10
            "11" -> modelFileSub11
            "12" -> modelFileSub12
            "13" -> modelFileSub13
            "14" -> modelFileSub14
            "15" -> modelFileSub15
            "17" -> modelFileSub17
            "18" -> modelFileSub18
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }
    }

    fun recordingFileSTFatigue(sub: String): File {
        return when (sub) {
            "01" -> recordingFileSub01STFatigue
            "02" -> recordingFileSub02STFatigue
            "03" -> recordingFileSub03STFatigue
            "05" -> recordingFileSub05STFatigue
            "06" -> recordingFileSub06STFatigue
            "07" -> recordingFileSub07STFatigue
            "08" -> recordingFileSub08STFatigue
            "09" -> recordingFileSub09STFatigue
            "10" -> recordingFileSub10STFatigue
            "11" -> recordingFileSub11STFatigue
            "12" -> recordingFileSub12STFatigue
            "13" -> recordingFileSub13STFatigue
            "14" -> recordingFileSub14STFatigue
            "15" -> recordingFileSub15STFatigue
            "17" -> recordingFileSub17STFatigue
            "18" -> recordingFileSub18STFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }
    }

    fun recordingFileDTFatigue(sub: String): File {
        return when (sub) {
            "01" -> recordingFileSub01DTFatigue
            "02" -> recordingFileSub02DTFatigue
            "03" -> recordingFileSub03DTFatigue
            "05" -> recordingFileSub05DTFatigue
            "06" -> recordingFileSub06DTFatigue
            "07" -> recordingFileSub07DTFatigue
            "08" -> recordingFileSub08DTFatigue
            "09" -> recordingFileSub09DTFatigue
            "10" -> recordingFileSub10DTFatigue
            "11" -> recordingFileSub11DTFatigue
            "12" -> recordingFileSub12DTFatigue
            "13" -> recordingFileSub13DTFatigue
            "14" -> recordingFileSub14DTFatigue
            "15" -> recordingFileSub15DTFatigue
            "17" -> recordingFileSub17DTFatigue
            "18" -> recordingFileSub18DTFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }
    }

    fun recordingFileSTNonFatigue(sub: String): File {
        return when (sub) {
            "01" -> recordingFileSub01STNonFatigue
            "02" -> recordingFileSub02STNonFatigue
            "03" -> recordingFileSub03STNonFatigue
            "05" -> recordingFileSub05STNonFatigue
            "06" -> recordingFileSub06STNonFatigue
            "07" -> recordingFileSub07STNonFatigue
            "08" -> recordingFileSub08STNonFatigue
            "09" -> recordingFileSub09STNonFatigue
            "10" -> recordingFileSub10STNonFatigue
            "11" -> recordingFileSub11STNonFatigue
            "12" -> recordingFileSub12STNonFatigue
            "13" -> recordingFileSub13STNonFatigue
            "14" -> recordingFileSub14STNonFatigue
            "15" -> recordingFileSub15STNonFatigue
            "17" -> recordingFileSub17STNonFatigue
            "18" -> recordingFileSub18STNonFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }
    }

    fun recordingFileDTNonFatigue(sub: String): File {
        return when (sub) {
            "01" -> recordingFileSub01DTNonFatigue
            "02" -> recordingFileSub02DTNonFatigue
            "03" -> recordingFileSub03DTNonFatigue
            "05" -> recordingFileSub05DTNonFatigue
            "06" -> recordingFileSub06DTNonFatigue
            "07" -> recordingFileSub07DTNonFatigue
            "08" -> recordingFileSub08DTNonFatigue
            "09" -> recordingFileSub09DTNonFatigue
            "10" -> recordingFileSub10DTNonFatigue
            "11" -> recordingFileSub11DTNonFatigue
            "12" -> recordingFileSub12DTNonFatigue
            "13" -> recordingFileSub13DTNonFatigue
            "14" -> recordingFileSub14DTNonFatigue
            "15" -> recordingFileSub15DTNonFatigue
            "17" -> recordingFileSub17DTNonFatigue
            "18" -> recordingFileSub18DTNonFatigue
            else -> throw IllegalArgumentException("Unknown sub $sub")
        }
    }

    companion object {
        class TrainingResult(
            val accuracyBeforeTraining: Float,
            val accuracyAfterTraining: Float,
            val confusionMatrixBeforeTraining: ConfusionMatrix,
            val confusionMatrixAfterTraining: ConfusionMatrix,
        )

        val subs = listOf(
            "01", "02", "03", "05", "06", "07", "08", "09",
            "10", "11", "12", "13", "14", "15", "17", "18"
        )
    }
}