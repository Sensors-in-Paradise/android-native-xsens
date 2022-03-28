package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class Object3D(val vertices: Array<Vec4>, var onObjectChanged: OnObjectChangedInterface? = null) {
    private val defaultVertices = vertices.map { it.clone() }

    fun scale(x: Float, y: Float, z: Float) {
        val m = Matrix4x4().apply { scale(x, y, z) }
        applyOnAllVertices(m)
    }
    fun translate(x: Float, y: Float, z: Float) {
        val m = Matrix4x4().apply { translate(x, y, z) }
        applyOnAllVertices(m)
    }

    fun rotate(x: Float, y: Float, z: Float) {
        val m = Matrix4x4().apply { this.rotateEuler(x, y, z) }
        applyOnAllVertices(m)
    }
    fun rotateRadians(x: Float, y: Float, z: Float) {
        val m = Matrix4x4().apply { this.rotateEuler(radiansToDegrees(x), radiansToDegrees(y), radiansToDegrees(z)) }
        applyOnAllVertices(m)
    }

    private val radiansToDegreesFactor = Math.PI.toFloat() / 180f
    private fun radiansToDegrees(radians: Float): Float {
        return (radiansToDegreesFactor * radians)
    }
    private fun applyOnAllVertices(m: Matrix4x4) {
        for (v in vertices) {
            v *= m
        }
        notifyVerticesChanged()
    }
    abstract fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> PointF)

    fun notifyVerticesChanged() {
        onObjectChanged?.onObjectChanged()
    }

    fun resetToDefaultState() {
        for (i in vertices.indices) {
            vertices[i].assign(defaultVertices[i])
        }
        notifyVerticesChanged()
    }
}
