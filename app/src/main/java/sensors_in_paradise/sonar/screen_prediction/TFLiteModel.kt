package sensors_in_paradise.sonar.screen_prediction

import org.tensorflow.lite.Interpreter
import java.io.File

class TFLiteModel(tfLiteModelFile: File){
    // TODO replace interpreter in PredictionScreen by this wrapper
    private val interpreter = Interpreter(tfLiteModelFile)
}