package sensors_in_paradise.sonar.screen_prediction

import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.nio.ByteBuffer

class TFLiteModel(tfLiteModelFile: File) {
    constructor(tfLiteModelFile: File, inputs: IntArray, outputSize: Int) : this(tfLiteModelFile) {
        this.interpreter = Interpreter(tfLiteModelFile).apply {
            resizeInput(0, inputs)
        }
        this.outputs = arrayOf(FloatArray(outputSize))
    }
    var hasMetadata: Boolean = false
    private lateinit var outputs: Array<FloatArray>
    private lateinit var interpreter: Interpreter
    private lateinit var extractor: MetadataExtractor

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
            }
        } catch (e: Exception) {
            Log.d("TFLiteModel-init", "Model doesn't have Metadata")
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
}
