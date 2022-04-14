/*
Code from: https://github.com/tensorflow/examples/tree/master/lite/examples/pose_estimation/android
==============================================================================
Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package sensors_in_paradise.sonar.page2.camera.pose_estimation

import android.graphics.*
import android.util.Log
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.BodyPart
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person
import kotlin.math.max

object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 9f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 6f

    /** The text size of the person id that will be displayed when the tracker is available.  */
    private const val PERSON_ID_TEXT_SIZE = 30f

    /** Distance from person id to the nose keypoint.  */
    private const val PERSON_ID_MARGIN = 6f

    /** Pair of keypoints to draw lines between.  */
    private val bodyJoints = listOf(
        Pair(BodyPart.NOSE, BodyPart.LEFT_EYE),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_EYE),
        Pair(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR),
        Pair(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR),
        Pair(BodyPart.NOSE, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    private fun rotate90Degrees(p: PointF, width: Int, height: Int, bm: Bitmap): PointF {
       val p_norm = PointF(p.x / bm.width.toFloat(), p.y / bm.height.toFloat())
       val p_norm_rot = PointF(1f-p_norm.y, p_norm.x)
       val p_rot = PointF(p_norm_rot.x * height.toFloat(), p_norm_rot.y * width.toFloat())
        //Log.d("CameraManager", "${p_norm_rot.x} - ${p_norm_rot.y}")
        return p_rot //PointF(p.x * (width.toFloat() / bm.width.toFloat()), p.y * (height.toFloat() / bm.height.toFloat()))
    }
    // Draw line and point indicate body pose
    fun drawBodyKeypoints(
        input: Bitmap,
        cv: Canvas,
        persons: List<Person>,
        isTrackerEnabled: Boolean = false
    ): Bitmap {
        val paintCircle = Paint().apply {
            strokeWidth = CIRCLE_RADIUS
            color = Color.BLUE
            style = Paint.Style.FILL
        }
        val paintLine = Paint().apply {
            strokeWidth = LINE_WIDTH
            color = Color.WHITE
            style = Paint.Style.STROKE
        }

        val paintText = Paint().apply {
            textSize = PERSON_ID_TEXT_SIZE
            color = Color.BLUE
            textAlign = Paint.Align.LEFT
        }

        val output = input.copy(Bitmap.Config.ARGB_8888, true)
        output.eraseColor(Color.TRANSPARENT)

        //val originalSizeCanvas = Canvas(output)
        val cv2 = cv
        cv2.drawColor(Color.GREEN, PorterDuff.Mode.CLEAR)
        cv2.drawLine(3f, 3f, 3f, cv2.height - 3f, paintLine)
        cv2.drawLine(3f, 3f, cv2.width - 3f, 3f, paintLine)
        cv2.drawLine(cv2.width - 3f, 3f, cv2.width - 3f, cv2.height - 3f, paintLine)
        cv2.drawLine(3f, cv2.height - 3f, cv2.width - 3f, cv2.height - 3f, paintLine)

        persons.forEach { person ->
            // draw person id if tracker is enable
            if (isTrackerEnabled) {
                person.boundingBox?.let {
                    val personIdX = max(0f, it.left)
                    val personIdY = max(0f, it.top)

                    cv2.drawText(
                        person.id.toString(),
                        personIdX,
                        personIdY - PERSON_ID_MARGIN,
                        paintText
                    )
                    cv2.drawRect(it, paintLine)
                }
            }
            bodyJoints.forEach {
                val pointA = rotate90Degrees(person.keyPoints[it.first.position].coordinate, cv2.width, cv2.height, input)
                val pointB = rotate90Degrees(person.keyPoints[it.second.position].coordinate, cv2.width, cv2.height, input)
                cv2.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
            }

            person.keyPoints.forEach { point ->
                val coordinate = rotate90Degrees(point.coordinate, cv2.width, cv2.height, input)
                cv2.drawCircle(
                    coordinate.x,
                    coordinate.y,
                    CIRCLE_RADIUS,
                    paintCircle
                )
            }
        }
        return output
    }
}
