package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Color

class Sensor3D : Cube() {
    init {
        val topSide = getTopSide()
        children.add(
            Plane(
                topSide[0].clone(),
                topSide[1].clone(),
                topSide[2].clone(),
                topSide[3].clone(),
                Color.valueOf(253f / 255f, 109f / 255f, 83f / 255f, 0.5f).toArgb()
            )
        )
        children.add(CoordinateSystem3D())
    }
}
