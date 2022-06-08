package sensors_in_paradise.sonar.screen_prediction

import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.nio.ByteBuffer
import kotlin.properties.Delegates

class TFLiteModel(tfLiteModelFile: File) {
    var hasMetadata: Boolean = false
    var windowSize by Delegates.notNull<Int>()
    private val frequency = 60
    private lateinit var outputs: Array<FloatArray>
    private lateinit var interpreter: Interpreter
    var extractor: MetadataExtractor
    private lateinit var labels: ArrayList<String>
    private lateinit var features: Array<String>

    init {
            val buffer = ByteBuffer.allocate(tfLiteModelFile.readBytes().size)
            buffer.put(tfLiteModelFile.readBytes())
            buffer.rewind()
            extractor = MetadataExtractor(buffer)
            if (hasMetadata()) {
                Log.d("TFLiteModel", "Metadata found")
                val inputs = extractor.getInputTensorShape(0)
                interpreter = Interpreter(tfLiteModelFile).apply {
                    resizeInput(0, inputs)
                }
                features = readFeatureFile().split("\n").toTypedArray()
                labels = ArrayList(readLabelsFile().split("\n"))
                outputs = arrayOf(FloatArray(extractor.getOutputTensorShape(0)[1]))
                windowSize = extractor.getInputTensorShape(0)[1]
            } else {
                Log.e("TFLiteModel", "No metadata found")
            }
    }

    val signatureKeys: Array<String> = interpreter.signatureKeys

    private fun hasMetadata(): Boolean {
        return extractor.hasMetadata().apply { hasMetadata = this }
    }

    fun close() {
        interpreter.close()
    }

    fun predict(sensorDataByteBuffer: ByteBuffer): FloatArray {
        interpreter.run(
            sensorDataByteBuffer,
            outputs
        )
        return outputs[0]
    }

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
        return features.map { it.substringAfterLast("_") }.distinct().toTypedArray()
    }

    fun getNumDevices(): Int {
        return getDeviceTags().size
    }

    fun getFeaturesToPredict(): Array<String> {
        return features
    }

    fun getWindowInSeconds(): Long {
        return 1000L * (windowSize / frequency)
    }

    fun runTraining(windows: Any, labels: Any): FloatArray {
        val inputs = HashMap<String, Any>()
        inputs["windows"] = windows
        inputs["labels"] = labels
        val outputs = HashMap<String, Any>()
        outputs["loss"] = FloatArray(1)
        interpreter.runSignature(inputs, outputs, "train")
        return outputs["loss"] as FloatArray
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
}
