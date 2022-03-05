package sensors_in_paradise.sonar.custom_views.stickman

import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec3

class Camera(center: Vec3, eye: Vec3, up: Vec3) {
    val lookAtMatrix = Matrix4x4.lookAt(eye, center, up)

}