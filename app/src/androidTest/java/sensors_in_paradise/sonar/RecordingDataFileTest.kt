package sensors_in_paradise.sonar

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import sensors_in_paradise.sonar.screen_prediction.TFLiteModel
import sensors_in_paradise.sonar.screen_recording.RecordingDataFile
import sensors_in_paradise.sonar.use_cases.UseCase
import java.io.File
import java.nio.FloatBuffer

class RecordingDataFileTest {
    private val assetContext = InstrumentationRegistry.getInstrumentation().context
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val recordingFile = File(appContext.cacheDir, "recordingData.csv")
    private val modelFile = File(appContext.cacheDir, "model_test.tflite")
    @Before
    fun init() {
        UseCase.extractFileFromAssets(assetContext, "0_orhan_1652085453257.csv", recordingFile)
        UseCase.extractFileFromAssets(assetContext, "resnet_model_test.tflite", modelFile)
    }

    @Test
    fun windowizeTest() {
        val features = arrayOf("Quat_Z_LF", "dq_W_LF", "dv[1]_LF").map { it.uppercase() }.toTypedArray()
        val windowSize = 90
        val data = RecordingDataFile(recordingFile)
        val startIndexes = data.getWindowStartIndexes(windowSize)

        assert(startIndexes.size > 0)
        var i = 1
        for (startIndex in startIndexes) {
            Log.d("RecordingDataFileTest-windowizeTest", "Working on window $i of ${startIndexes.size}")
            val (window, activity) = data.getWindowAtIndex(startIndex, windowSize, features)
            assert(window.size == features.size)

            // Test if compiling the window into a float buffer runs through
            window.compileWindow()
            i++
        }
    }

    @Test
    fun predictionPipelineTest() {
        val model = TFLiteModel(modelFile)

        val features = model.getFeaturesToPredict().map { it.uppercase() }.toTypedArray()

        val data = RecordingDataFile(recordingFile)
        val startIndexes = data.getWindowStartIndexes(model.windowSize)
        var correctPredictions = 0
        assert(startIndexes.size > 0)

        var i = 1
        for (startIndex in startIndexes) {
            val (window, activity) = data.getWindowAtIndex(startIndex, model.windowSize, features)
            assert(window.size == features.size)

            // Test if compiling the window into a float buffer runs through
            val input = window.compileWindow()
            val prediction = model.runInfer(input)
            val predictedLabel = model.convertPredictionToLabel(prediction)
            if (predictedLabel == activity) {
                correctPredictions++
            }
            Log.d("RecordingDataFileTest-predictionPipelineTest", "Working on window $i of ${startIndexes.size}")
            i++
        }
        val accuracy = (correctPredictions * 100 / startIndexes.size)
        Log.d("RecordingDataFileTest", "Prediction accuracy on the example recording: $accuracy%")
    }
    @Test
    fun trainingPipelineTest() {
        val model = TFLiteModel(modelFile)

        val features = model.getFeaturesToPredict().map { it.uppercase() }.toTypedArray()

        val data = RecordingDataFile(recordingFile)
        val startIndexes = data.getWindowStartIndexes(model.windowSize)
        val numFeatures = features.size
        val numLabels = model.getLabelsMap().size
        assert(startIndexes.size > 0)

        val batchSize = startIndexes.size
        val trainingInputBuffer = FloatBuffer.allocate(batchSize*model.windowSize*numFeatures)
        val trainingOutputBuffer = FloatBuffer.allocate(batchSize*numLabels)
        // Training
        for ((i,startIndex) in startIndexes.withIndex()) {
            val (window, activity) = data.getWindowAtIndex(startIndex, model.windowSize, features)
            assert(window.size == features.size)

            // Test if compiling the window into a float buffer runs through
            val windowData = window.compileWindow()
            val labelOneHot = model.convertLabelToOneHotEncoding(activity)
            trainingInputBuffer.put(windowData)
            trainingOutputBuffer.put(labelOneHot)

            model.runTraining(windowData, labelOneHot)
            Log.d("RecordingDataFileTest-predictionPipelineTest", "Working on window $i of ${startIndexes.size}")
        }



        // Prediction
        var correctPredictions = 0
        for ((i,startIndex) in startIndexes.withIndex()) {
            val (window, activity) = data.getWindowAtIndex(startIndex, model.windowSize, features)
            assert(window.size == features.size)

            // Test if compiling the window into a float buffer runs through
            val input = window.compileWindow()
            val prediction = model.runInfer(input)
            val predictedLabel = model.convertPredictionToLabel(prediction)
            if (predictedLabel == activity) {
                correctPredictions++
            }
            Log.d("RecordingDataFileTest-predictionPipelineTest", "Working on window $i of ${startIndexes.size}")
        }
        val accuracyAfterTraining = (correctPredictions * 100 / startIndexes.size)
        Log.d("RecordingDataFileTest", "Prediction accuracy on the example recording after training: $accuracyAfterTraining%")
    }


}
