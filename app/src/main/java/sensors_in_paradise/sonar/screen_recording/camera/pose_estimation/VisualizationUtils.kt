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

package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import android.graphics.*
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.BodyPart
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.KeyPoint
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.Person
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.PoseSequence

object VisualizationUtils {
    /** Radius of circle used to draw keypoints.  */
    private const val CIRCLE_RADIUS = 8f

    /** Width of line used to connected two keypoints.  */
    private const val LINE_WIDTH = 6f

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

    enum class Transformation {
        NORMALIZE,
        ROTATE90,
        PROJECT_ON_CANVAS,
    }

    private fun normalizePoint(p: PointF, inputSize: PointF): PointF {
        return PointF(p.x / inputSize.x, p.y / inputSize.y)
    }

    private fun rotatePoint90(p: PointF): PointF {
        return PointF(1f - p.y, p.x)
    }

    private fun projectPointOnCanvas(
        p: PointF,
        inputSize: PointF,
        outputSize: PointF,
        isRotated: Boolean
    ): PointF {
        // p should be normalized to [0,1]
        val actualInputSize = if (isRotated) PointF(inputSize.y, inputSize.x) else inputSize
        val heightRatio = (actualInputSize.y * outputSize.x) / (outputSize.y * actualInputSize.x)
        val pScaled = PointF(p.x, (p.y * heightRatio) - ((heightRatio - 1f) / 2f))
        return PointF(pScaled.x * outputSize.x, pScaled.y * outputSize.y)
    }

    fun transformKeyPoints(
        persons: List<Person>,
        bitmap: Bitmap?,
        canvas: Canvas?,
        transformation: Transformation,
        isRotated: Boolean = false
    ) {
        val inputSize = bitmap?.let { PointF(bitmap.width.toFloat(), bitmap.height.toFloat()) }
        val outputSize = canvas?.let { PointF(canvas.width.toFloat(), canvas.height.toFloat()) }
        persons.forEach { person ->
            person.keyPoints.forEach { keyPoint ->
                when (transformation) {
                    Transformation.NORMALIZE -> keyPoint.coordinate =
                        normalizePoint(keyPoint.coordinate, inputSize!!)

                    Transformation.ROTATE90 -> keyPoint.coordinate =
                        rotatePoint90(keyPoint.coordinate)

                    Transformation.PROJECT_ON_CANVAS -> keyPoint.coordinate =
                        projectPointOnCanvas(
                            keyPoint.coordinate,
                            inputSize!!,
                            outputSize!!,
                            isRotated
                        )
                }
            }
        }
    }

    @Suppress("ComplexCondition")
    fun interpolatePersons(
        poseSequence: PoseSequence,
        floorIndex: Int,
        timeStamp: Long,
        timeMargin: Long = 800
    ): List<Person> {
        val lowerTimeStamp = poseSequence.timeStamps.getOrNull(floorIndex) ?: Long.MAX_VALUE
        val upperTimeStamp = poseSequence.timeStamps.getOrNull(floorIndex + 1)
        val lowerPerson = poseSequence.personsArray.getOrNull(floorIndex)?.getOrNull(0)
        val upperPerson = poseSequence.personsArray.getOrNull(floorIndex + 1)?.getOrNull(0)
        if (lowerPerson == null ||
            timeStamp < lowerTimeStamp ||
            timeStamp > upperTimeStamp ?: Long.MAX_VALUE ||
            (timeStamp - lowerTimeStamp) > timeMargin
        ) { // No prior sample / TimeStamp inconsistent / big gap to prior AND next sample
            return listOf<Person>()
        } else if (upperPerson == null ||
            upperTimeStamp == null ||
            (upperTimeStamp - lowerTimeStamp) > timeMargin
        ) { // No next sample / big gap to next sample
            return listOf(lowerPerson)
        } else {
            val interpolateFactor =
                (timeStamp - lowerTimeStamp).toFloat() / (upperTimeStamp - lowerTimeStamp).toFloat()
            val person = Person(
                lowerPerson.id,
                BodyPart.values().map { bp ->
                    val lowerP = lowerPerson.keyPoints[bp.position].coordinate
                    val upperP = upperPerson.keyPoints[bp.position].coordinate

                    val x = lowerP.x + (upperP.x - lowerP.x) * interpolateFactor
                    val y = lowerP.y + (upperP.y - lowerP.y) * interpolateFactor

                    KeyPoint(bp, PointF(x, y), 1f)
                }.toList(),
                lowerPerson.boundingBox,
                lowerPerson.score
            )
            return listOf(person)
        }
    }

    // Draw line and point indicate body pose
    fun drawBodyKeyPoints(
        persons: List<Person>,
        canvas: Canvas,
        clearColor: Int? = null,
        circleColor: Int = Color.BLACK,
        lineColor: Int = Color.WHITE,
        circleRadius: Float = CIRCLE_RADIUS,
        lineWidth: Float = LINE_WIDTH
    ) {
        val paintCircle = Paint().apply {
            strokeWidth = circleRadius
            color = circleColor
            style = Paint.Style.FILL_AND_STROKE
        }
        val paintLine = Paint().apply {
            strokeWidth = lineWidth
            color = lineColor
            style = Paint.Style.STROKE
        }

        if (clearColor == null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        } else {
            canvas.drawColor(clearColor)
        }

        persons.forEach { person ->
            bodyJoints.forEach {
                val pointA = person.keyPoints[it.first.position].coordinate
                val pointB = person.keyPoints[it.second.position].coordinate
                canvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, paintLine)
            }

            person.keyPoints.forEach { point ->
                val coordinate = point.coordinate
                canvas.drawCircle(
                    coordinate.x,
                    coordinate.y,
                    circleRadius,
                    paintCircle
                )
            }
        }
    }
}
