package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

interface DrawableObject3DInterface {
    fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> PointF)
}
