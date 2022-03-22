package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class Object3D(val vertices:Array<Vec4>) {

    fun scale(x: Float, y: Float, z: Float){
        val m = Matrix4x4().apply { scale(x,y,z)}
        applyOnAllVertices(m)
    }
    fun translate(x: Float, y: Float, z: Float){
        val m = Matrix4x4().apply { translate(x,y,z)}
        applyOnAllVertices(m)
    }
    private fun applyOnAllVertices(m: Matrix4x4){
        for (v in vertices){
            v *= m
        }
    }
    abstract fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> PointF)
}
