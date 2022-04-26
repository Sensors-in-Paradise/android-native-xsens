/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

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

package sensors_in_paradise.sonar.page2.camera.pose_estimation.data

import android.graphics.PointF
import android.graphics.RectF

data class Person(
    var id: Int = -1, // default id is -1
    var keyPoints: List<KeyPoint>,
    val boundingBox: RectF? = null, // Only MoveNet MultiPose return bounding box.
    val score: Float
) {
    fun copy(): Person {
        return Person(id, keyPoints.map { it.copy() }, boundingBox, score)
    }

    companion object {
        fun getNULLPerson(): Person {
            return Person(
                -1,
                BodyPart.values().map { bp -> KeyPoint(bp, PointF(-1f, -1f), 0f) },
                RectF(),
                0f
            )
        }
    }
}
