package sensors_in_paradise.sonar.screen_prediction

import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.nio.ByteBuffer
import java.nio.FloatBuffer

class TFLiteModel @Throws(InvalidModelMetadata::class) constructor(tfLiteModelFile: File) {
    class InvalidModelMetadata(message: String) : Exception(message)

    val windowSize: Int
    private val frequency = 60
    private val output: FloatBuffer
    private val interpreter: Interpreter
    private val extractor: MetadataExtractor
    private val labels: ArrayList<String>
    private val features: Array<String>
    val signatureKeys: Array<String>

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

    fun getDeviceTags(): Array<String> {
        return features.map { it.substringAfterLast("_") }.distinct().filter { it != "" }
            .toTypedArray()
    }

    fun getNumDevices(): Int {
        return getDeviceTags().size
    }

    fun getFeaturesToPredict(): Array<String> {
        return features
    }

    fun getWindowInMilliSeconds(): Long {
        return 1000L * (windowSize / frequency)
    }

    fun runInfer(window: FloatBuffer): FloatArray {
        val inputs = HashMap<String, Any>()
        inputs["input_window"] = window.rewind()
        val outputs = HashMap<String, Any>()
        outputs["output"] = this.output
        interpreter.runSignature(inputs, outputs, "infer")
        output.rewind()
        return output.array()
    }

    fun runTraining(windows: FloatBuffer, labels: FloatArray): FloatArray {
        val numFeatures = features.size
        val batchSize = windows.capacity() / (windowSize * numFeatures)
        Log.d("Test-TFLiteModel - runTraining", "Running training on batch size $batchSize")
        val inputs = HashMap<String, Any>()
        inputs["input_windows"] = windows
        inputs["labels"] = labels
        val outputs = HashMap<String, Any>()
        val loss = FloatBuffer.allocate(1)
        loss.rewind()
        outputs["loss"] = loss

        val getInputShape: ()->String = {
            interpreter.getInputTensorFromSignature("input_windows", "train").shapeSignature().joinToString()
        }
        Log.d(
            "Test-TFLiteModel-runTraining",
            "Input tensor shape before: ${
                getInputShape()
            }"
        )

        //interpreter.resizeInput(interpreter.getInputIndex("input_windows"), intArrayOf(batchSize, windowSize, numFeatures), true)
        //interpreter.allocateTensors()
        Log.d(
            "Test-TFLiteModel-runTraining",
            "Input tensor shape after: ${
                getInputShape()
            }"
        )
        windows.rewind()

        interpreter.runSignature(inputs, outputs, "train")
        return loss as FloatArray
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
}
