package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.google.mediapipe.solutions.hands.HandLandmark
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult


class HandDetector {

    fun create() {
        val handsOptions = HandsOptions.builder()
            .setMaxNumHands(2)
            .setRunOnGpu(true).build()
            .setStaticImageMode(true)
        val handDetector = Hands(this, handsOptions)


        handDetector

        handDetector.setResultListener { handsResult: HandsResult ->
            if (handsResult.multiHandLandmarks().isEmpty()) {
                return@setResultListener
            }
            val width = handsResult.inputBitmap().width
            val height = handsResult.inputBitmap().height
            val wristLandmark =
                handsResult.multiHandLandmarks()[0].landmarkList[HandLandmark.WRIST]
            Log.i(
                TAG, String.format(
                    "MediaPipe Hand wrist coordinates (pixel values): x=%f, y=%f",
                    wristLandmark.x * width, wristLandmark.y * height
                )
            )
            // Request canvas drawing.
            imageView.setHandsResult(handsResult)
            UiThreadStatement.runOnUiThread(Runnable { imageView.update() })
        }
        handDetector.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(
                TAG,
                "MediaPipe Hands error:$message"
            )
        }
    }

}