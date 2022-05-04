package sensors_in_paradise.sonar.screen_prediction

import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer

class TFLiteModel(tfLiteModelFile: File, inputShape: IntArray, outputSize: Int) {
    private val outputs = arrayOf(FloatArray(outputSize))
    // TODO replace interpreter in PredictionScreen by this wrapper
    private val interpreter = Interpreter(tfLiteModelFile).apply {
        resizeInput(0, inputShape)
    }
    val signatureKeys: Array<String> = interpreter.signatureKeys
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
