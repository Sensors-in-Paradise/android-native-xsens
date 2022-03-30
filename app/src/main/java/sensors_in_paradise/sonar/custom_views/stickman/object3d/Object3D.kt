package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.Canvas
import android.graphics.PointF
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4

abstract class Object3D(protected val vertices: Array<Vec4>, private val children: ArrayList<Object3D> = ArrayList(), var onObjectChanged: OnObjectChangedInterface? = null) {
    private val defaultVertices = vertices.map { it.clone() }

    fun scale(x: Float, y: Float, z: Float) {
        val m = Matrix4x4().apply { scale(x, y, z) }
        applyOnAllVertices(m)
    }
    fun translate(x: Float, y: Float, z: Float) {
        val m = Matrix4x4().apply { translate(x, y, z) }
        applyOnAllVertices(m)
    }
    fun rotate(xDegrees: Float, yDegrees: Float, zDegrees: Float) {
        val m = Matrix4x4().apply { this.rotate(xDegrees, yDegrees, zDegrees) }
        applyOnAllVertices(m)
    }
    fun rotateEuler(xDegrees: Float, yDegrees: Float, zDegrees: Float) {
        val m = Matrix4x4().apply { this.rotateEuler(xDegrees, yDegrees, zDegrees) }
        applyOnAllVertices(m)
    }
    fun rotateEulerRadians(x: Float, y: Float, z: Float) {
        val m = Matrix4x4().apply { this.rotateEuler(radiansToDegrees(x), radiansToDegrees(y), radiansToDegrees(z)) }
        applyOnAllVertices(m)
    }

    private fun radiansToDegrees(radians: Float): Float {
        return (radiansToDegreesFactor * radians)
    }
    private fun applyOnAllVertices(m: Matrix4x4, applyOnChildren: Boolean=true,shouldNotifyThatVerticesChanged:Boolean = true) {
        for (v in vertices) {
            v *= m
        }
        if(applyOnChildren) {
            for (child in children) {
                child.applyOnAllVertices(m, false)
            }
        }
        if(shouldNotifyThatVerticesChanged) {
            notifyVerticesChanged()
        }
    }
    fun draw(canvas: Canvas, projectPoint: (p: Vec4) -> PointF){
        drawSelf(canvas, projectPoint)
        for(child in children){
            child.draw(canvas, projectPoint)
        }
    }
    protected abstract fun drawSelf(canvas: Canvas, projectPoint: (p: Vec4) -> PointF)

    fun notifyVerticesChanged() {
        onObjectChanged?.onObjectChanged()
    }

    fun resetToDefaultState() {
        for (i in vertices.indices) {
            vertices[i].assign(defaultVertices[i])
        }
        notifyVerticesChanged()
    }
    companion object{
        private const val radiansToDegreesFactor = Math.PI.toFloat() / 180f
    }
}
