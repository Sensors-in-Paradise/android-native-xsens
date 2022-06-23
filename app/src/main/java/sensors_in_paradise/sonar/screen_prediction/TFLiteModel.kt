package sensors_in_paradise.sonar.screen_prediction

import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import kotlin.Array as Array1

//TODO: move all ML related classes into new package (they are used across multiple screens, not only prediction screen)
class TFLiteModel @Throws(InvalidModelMetadata::class) constructor(tfLiteModelFile: File) {
    class InvalidModelMetadata(message: String) : Exception(message)

    val windowSize: Int
    private val frequency = 60
    private val output: FloatBuffer
    private val interpreter: Interpreter
    private val extractor: MetadataExtractor
    private val labels: ArrayList<String>
    private val features: Array1<String>
    val signatureKeys: Array1<String>

    init {
        val buffer = ByteBuffer.allocate(tfLiteModelFile.readBytes().size)
        buffer.put(tfLiteModelFile.readBytes())
        buffer.rewind()
        extractor = MetadataExtractor(buffer)
        if (!hasMetadata()) {
            throw InvalidModelMetadata("The model ${tfLiteModelFile.name} does not have any metadata")
        }
        val inputs = extractor.getInputTensorShape(0)
        interpreter = Interpreter(tfLiteModelFile).apply {
            resizeInput(0, inputs)
        }
        signatureKeys = interpreter.signatureKeys
        features = readFeatureFile().split("\n").filter { it.isNotEmpty() }.toTypedArray()
        labels = ArrayList(readLabelsFile().split("\n").filter { it.isNotEmpty() })
        output = FloatBuffer.allocate(labels.size)
        windowSize = extractor.getInputTensorShape(0)[1]
    }

    private fun hasMetadata(): Boolean {
        return extractor.hasMetadata()
    }

    fun close() {
        interpreter.close()
    }
    /*
    fun predict(sensorDataByteBuffer: ByteBuffer): FloatArray {
        interpreter.run(
            sensorDataByteBuffer,
            output
        )
        return output[0]
    }*/

    private fun readFeatureFile(): String {
        val inputStream = extractor.getAssociatedFile("features.txt")
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun readLabelsFile(): String {
        val inputStream = extractor.getAssociatedFile("labels.txt")
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun getLabelsMap(): Map<Int, String> {
        return labels.mapIndexed { index, label ->
            index to label
        }.toMap()
    }

    fun getNumOutputClasses(): Int {
        return labels.size
    }

    fun getDeviceTags(): Array1<String> {
        return features.map { it.substringAfterLast("_") }.distinct().filter { it != "" }
            .toTypedArray()
    }

    fun getNumDevices(): Int {
        return getDeviceTags().size
    }

    fun getFeaturesToPredict(): Array1<String> {
        return features
    }

    fun getWindowInMilliSeconds(): Long {
        return 1000L * (windowSize / frequency)
    }

    @Deprecated(
        "This function seems to fail when runInfer(windows: Array<Array<FloatArray>>) has been executed before with multiple windows, altering the input buffer size.",
        ReplaceWith("runInfer(windows: Array<Array<FloatArray>>)")
    )
    fun infer(window: FloatBuffer): FloatArray {
        // TODO: This function seems to fail when runInfer(windows: Array<Array<FloatArray>>) has been executed before with multiple windows (Maybe delete this one?)
        val inputs = HashMap<String, Any>()
        inputs["input_window"] = window.rewind()
        val outputs = HashMap<String, Any>()
        outputs["output"] = this.output
        interpreter.runSignature(inputs, outputs, "infer")
        output.rewind()
        return output.array()
    }

    fun infer(windows: Array1<Array1<FloatArray>>): Array1<FloatArray> {
        val inputs = HashMap<String, Any>()
        inputs["input_window"] = windows
        val outputs = HashMap<String, Any>()
        Log.d("TFLiteModel-runInfer", "Output buffer capacity: ${labels.size * windows.size}")
        val output = Array1(windows.size) { FloatArray(labels.size) }
        outputs["output"] = output
        interpreter.runSignature(inputs, outputs, "infer")
        return output
    }

    fun evaluate(
        batches: Collection<Batch>,
        progressCallback: ((Int, Int) -> Unit)? = null
    ): Float {
        var accuracy = 0f
        for ((index, batch) in batches.withIndex()) {
            val interBatchProgress = (index * 100) / batches.size
            val batchAccuracy = evaluate(batch) { intraBatchProgress ->
                progressCallback?.let {
                    it(interBatchProgress, intraBatchProgress)
                }
            }
            accuracy += batchAccuracy / batches.size
        }
        return accuracy
    }

    fun evaluate(batch: Batch, progressCallback: ((Int) -> Unit)? = null): Float {
        val (windows, labels) = batch.compile(this, progressCallback)
        val prediction = infer(windows)
        return getAccuracyFromPredictions(prediction, labels)
    }

    fun train(batch: Batch, progressCallback: ((Int) -> Unit)? = null): Float {
        val (windows, labels) = batch.compile(this, progressCallback)
        return train(windows, labels)
    }
    private fun toPercentage(v: Int,  max: Int,min: Int=0):Int{
        return (v*100)/(max-min)
    }
    /*Returns an array of losses with one entry per epoch*/
    fun train(
        batches: Collection<Batch>,
        epochs: Int = 1,
        progressCallback: ((Int, Int, Int) -> Unit)? = null
    ): FloatArray {
        val losses = FloatArray(epochs)
        for (e in 0 until epochs) {
            var epochLoss = 0f
            for ((batchIndex, batch) in batches.withIndex()) {
                val batchLoss = train(batch) { intraBatchProgress ->
                    progressCallback?.let {
                        it(
                            toPercentage(e, epochs),
                            toPercentage(batchIndex, batches.size),
                            intraBatchProgress
                        )
                    }
                }
                epochLoss += batchLoss / batches.size
            }
            losses[e] = epochLoss
        }
        return losses
    }

    fun train(windows: Array1<Array1<FloatArray>>, labels: Array1<FloatArray>): Float {
        if (windows.isEmpty()) {
            throw java.lang.IllegalArgumentException("Can't run training on an empty windows array")
        }
        val numFeatures = features.size
        if (windows.size != labels.size) {
            throw java.lang.IllegalArgumentException("Window size ${windows.size} is different to number of labels ${labels.size}")
        }
        if (windows[0].size != windowSize) {
            throw java.lang.IllegalArgumentException("Size of provided windows ${windows[0].size} is different to expected window size: $windowSize")
        }
        if (windows[0][0].size != numFeatures) {
            throw java.lang.IllegalArgumentException("Number of provided features ${windows[0][0].size} inside the windows is different to expected number of features: $numFeatures")
        }
        val batchSize = windows.size
        Log.d("Test-TFLiteModel - runTraining", "Running training on batch size $batchSize")
        val inputs = HashMap<String, Any>()
        inputs["input_windows"] = windows
        inputs["labels"] = labels
        val outputs = HashMap<String, Any>()
        val loss = FloatBuffer.allocate(1)
        loss.rewind()
        outputs["loss"] = loss
        interpreter.runSignature(inputs, outputs, "train")
        return loss.array()[0]
    }

    fun runSave(file: File) {
        val inputs = HashMap<String, Any>()
        val outputs = HashMap<String, Any>()
        val checkpointPath = file.absolutePath
        inputs["checkpoint_path"] = checkpointPath
        interpreter.runSignature(inputs, outputs, "save")
    }

    fun runRestore(file: File) {
        val inputs = HashMap<String, Any>()
        val outputs = HashMap<String, Any>()
        val checkpointPath = file.absolutePath
        inputs["checkpoint_path"] = checkpointPath
        interpreter.runSignature(inputs, outputs, "restore")
    }

    @Throws(IllegalArgumentException::class)
    fun convertPredictionToLabel(prediction: FloatArray): String {
        val labels = getLabelsMap()
        if (labels.size != prediction.size) {
            throw IllegalArgumentException(
                "Number of items in prediction ${prediction.size} " +
                        "is different to number of labels in this model ${labels.size}"
            )
        }
        val index = prediction.indices.maxByOrNull {
            prediction[it]
        }!!
        return labels[index]!!
    }

    @Throws(IllegalArgumentException::class)
    fun convertLabelToOneHotEncoding(label: String): FloatArray {
        val labels = getLabelsMap()
        if (!labels.containsValue(label)) {
            throw IllegalArgumentException(
                "Label $label" +
                        "is not in labels of model: $labels"
            )
        }
        val index = labels.filter { label == it.value }.keys.first()

        val result = FloatArray(labels.size)
        result[index] = 1f
        return result
    }

    fun getAccuracyFromPredictions(
        predictions: Array1<FloatArray>,
        actualLabels: Array1<String>
    ): Float {
        if (predictions.size != actualLabels.size) {
            throw IllegalArgumentException("Size of predictions (${predictions.size}) is unequals to size of actualLabels (${actualLabels.size})")
        }
        var correctPredictions = 0f
        for (i in predictions.indices) {
            val prediction = predictions[i]
            val label = actualLabels[i]
            if (convertPredictionToLabel(prediction) == label) {
                correctPredictions++
            }
        }
        return correctPredictions / predictions.size
    }

    fun getAccuracyFromPredictions(
        predictions: Array1<FloatArray>,
        actualLabels: Array1<FloatArray>
    ): Float {
        if (predictions.size != actualLabels.size) {
            throw IllegalArgumentException("Size of predictions (${predictions.size}) is unequals to size of actualLabels (${actualLabels.size})")
        }
        var correctPredictions = 0f
        for (i in predictions.indices) {
            val prediction = predictions[i]
            val label = actualLabels[i]
            if (convertPredictionToLabel(prediction) == convertPredictionToLabel(label)) {
                correctPredictions++
            }
        }
        return correctPredictions / predictions.size
    }
    // TODO: add functions to run inference and training on Batch instance
}
