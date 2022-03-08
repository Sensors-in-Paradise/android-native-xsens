package sensors_in_paradise.sonar.custom_views.stickman.object3D

import android.graphics.Canvas
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec2
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class Object3D {
    abstract fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> Vec2)
}