package sensors_in_paradise.sonar.screen_prediction

import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import org.tensorflow.lite.support.metadata.schema.NormalizationOptions
import java.io.File
import java.nio.ByteBuffer
import kotlin.properties.Delegates

class TFLiteModel(tfLiteModelFile: File) {
    constructor(tfLiteModelFile: File, inputs: IntArray, outputSize: Int) : this(tfLiteModelFile) {
        this.interpreter = Interpreter(tfLiteModelFile).apply {
            resizeInput(0, inputs)
        }
        this.outputs = arrayOf(FloatArray(outputSize))
    }

    var hasMetadata: Boolean = false
    var windowSize by Delegates.notNull<Int>()
    private val frequency = 60
    private val normalizationOptions = arrayListOf("mean", "std")
    private lateinit var outputs: Array<FloatArray>
    private lateinit var interpreter: Interpreter
    lateinit var extractor: MetadataExtractor
    private lateinit var sensors: ArrayList<String>
    private lateinit var labels: ArrayList<String>
    private lateinit var sensorData: ArrayList<String>

    init {
        try {
            val buffer = ByteBuffer.allocate(tfLiteModelFile.readBytes().size)
            buffer.put(tfLiteModelFile.readBytes())
            buffer.rewind()
            extractor = MetadataExtractor(buffer)
            if (hasMetadata()) {
                val inputs = extractor.getInputTensorShape(0)
                outputs = arrayOf(FloatArray(extractor.getOutputTensorShape(0)[0]))
                interpreter = Interpreter(tfLiteModelFile).apply {
                    resizeInput(0, inputs)
                }
                sensors = ArrayList(readSensorFile().split("\n"))
                sensorData = ArrayList(readSensorDataFile().split("\n"))
                labels = ArrayList(readLabelsFile().split("\n"))
                windowSize = extractor.getInputTensorShape(0)[1]
            }
        } catch (e: Exception) {
            Log.d(
                "TFLiteModel-init",
                "Model doesn't have Metadata or is missing sensor.txt, sensor_data.txt, or labels.txt"
            )
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

    private fun readSensorFile(): String {
        val inputStream = extractor.getAssociatedFile("sensor.txt")
        Log.d(
            "PredictionScreen-readFeatureFile",
            inputStream.bufferedReader().use { it.readText() })
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun readSensorDataFile(): String {
        val inputStream = extractor.getAssociatedFile("sensor_data.txt")
        Log.d(
            "PredictionScreen-readFeatureFile",
            inputStream.bufferedReader().use { it.readText() })
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun readLabelsFile(): String {
        val inputStream = extractor.getAssociatedFile("labels.txt")
        Log.d(
            "PredictionScreen-readFeatureFile",
            inputStream.bufferedReader().use { it.readText() })
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun getLabelsMap(): Map<Int, String> {
        return labels.mapIndexed { index, label ->
            index to label
        }.toMap()
    }

    fun getNumDevices(): Int {
        return sensors.size
    }

    fun getSensorDataToPredict(): ArrayList<String> {
        return sensorData
    }

    fun getPredictionInterval(): Long {
        return 1000L * (windowSize / frequency)
    }

    fun parseNormalizationOptions(): {
        /*
        sensors
            mean
                acc
                    x
                    y
                    z
                gyr
                    x
                    y
                    z
            std
                acc
                    x
                    y
                    z
                gyr
                    x
                    y
                    z
        */
        val inputMeta = extractor.getInputTensorQuantizationParams(0)
        return false
    }
}
