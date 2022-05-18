package sensors_in_paradise.sonar.screen_prediction

import android.util.Log
import org.json.JSONObject
import org.json.JSONTokener
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.BufferedReader
import java.io.File
import java.nio.ByteBuffer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.properties.Delegates

class TFLiteModel(tfLiteModelFile: File) {
    constructor(tfLiteModelFile: File, inputs: IntArray, outputSize: Int) : this(tfLiteModelFile) {
        this.interpreter = Interpreter(tfLiteModelFile).apply {
            resizeInput(0, inputs)
        }
        this.outputs = arrayOf(FloatArray(outputSize))
    }

    var hasMetadata: Boolean = false
    private var windowSize by Delegates.notNull<Int>()
    private val frequency = 60
    private lateinit var outputs: Array<FloatArray>
    private lateinit var interpreter: Interpreter
    var extractor: MetadataExtractor
    private lateinit var sensors: ArrayList<String>
    private lateinit var labels: ArrayList<String>
    private lateinit var sensorData: ArrayList<String>
    lateinit var normalizationParams: HashMap<String, HashMap<String, ArrayList<Double>>>

    init {
        //try {
            val buffer = ByteBuffer.allocate(tfLiteModelFile.readBytes().size)
            buffer.put(tfLiteModelFile.readBytes())
            buffer.rewind()
            extractor = MetadataExtractor(buffer)
            if (hasMetadata()) {
                Log.d("TFLiteModel", "Metadata found")
                val inputs = extractor.getInputTensorShape(0)
                outputs = arrayOf(FloatArray(extractor.getOutputTensorShape(0)[0]))
                interpreter = Interpreter(tfLiteModelFile).apply {
                    resizeInput(0, inputs)
                }
                sensors = ArrayList(readSensorFile().split("\n"))
                sensorData = ArrayList(readSensorDataFile().split("\n"))
                labels = ArrayList(readLabelsFile().split("\n"))
                windowSize = extractor.getInputTensorShape(0)[1]
                normalizationParams = parseNormalizationParams()
            } else {
                Log.e("TFLiteModel", "No metadata found")
            }
        //} catch (e: Exception) {
        //    Log.d(
        //        "TFLiteModel-init",
        //        "Model doesn't have Metadata or is missing sensor.txt, sensor_data.txt, labels.txt or normalization_params.txt"
        //   )
        //}
    }

    private fun parseNormalizationParams(): HashMap<String, HashMap<String, ArrayList<Double>>> {
        val string =
            extractor.getAssociatedFile("normalization_params.txt").bufferedReader().use { it.readText() }
        val jsonObject = JSONTokener(string).nextValue() as JSONObject
        val paramsDictionary = HashMap<String, HashMap<String, ArrayList<Double>>>()
        // fill Hashmap with normalization params. The sensor_data is in order.
        for (sensor in sensors) {
            val sensorObject = jsonObject.getJSONObject(sensor)
            paramsDictionary[sensor] = HashMap()
            paramsDictionary[sensor]?.set("std", ArrayList())
            paramsDictionary[sensor]?.set("mean", ArrayList())

            for (sensor_data in sensorData) {
                val std = sensorObject.getJSONArray("std_$sensor_data")
                val mean = sensorObject.getJSONArray("mean_$sensor_data")
                paramsDictionary[sensor]?.get("std")?.apply { add(std.getDouble(0))
                    add(std.getDouble(1))
                    add(std.getDouble(2))
                }
                paramsDictionary[sensor]?.get("mean")?.apply { add(mean.getDouble(0))
                    add(mean.getDouble(1))
                    add(mean.getDouble(2))
                }
            }
        }
        return paramsDictionary
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
        val inputStream = extractor.getAssociatedFile("sensors.txt")
        return inputStream.bufferedReader().use { it.readText() }
    }

    private fun readSensorDataFile(): String {
        val inputStream = extractor.getAssociatedFile("sensor_data.txt")
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

    fun getNumDevices(): Int {
        return sensors.size
    }

    fun getSensorDataToPredict(): ArrayList<String> {
        return sensorData
    }

    fun getPredictionInterval(): Long {
        return 1000L * (windowSize / frequency)
    }
}
