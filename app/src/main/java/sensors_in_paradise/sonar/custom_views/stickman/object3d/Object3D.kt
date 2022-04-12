package sensors_in_paradise.sonar.custom_views.stickman.object3d

import android.graphics.*
import sensors_in_paradise.sonar.custom_views.stickman.math.Matrix4x4
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec3
import sensors_in_paradise.sonar.custom_views.stickman.math.Vec4
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

abstract class Object3D(
    protected val vertices: Array<Vec4>,
    protected val children: ArrayList<Object3D> = ArrayList(),
    var onObjectChanged: OnObjectChangedInterface? = null,
    center: Vec3 = Vec3(0f, 0f, 0f),
) {
    private val localMatrix = Matrix4x4().apply {
        // translate(center.x, center.y, center.z)
    }
    private val worldMatrix = Matrix4x4().apply {
        translate(center.x, center.y, center.z)
    }
    private val defaultLocalMatrix = localMatrix.clone()
    private val defaultWorldMatrix = worldMatrix.clone()
    var drawVertexPositionsForDebugging = false
    private val vertexPositionsDebuggingHeader = "i_|_x__|_y__|_z__"
    private val debugTextBounds = Rect().apply {
        debugTextPaint.getTextBounds(
            vertexPositionsDebuggingHeader,
            0,
            vertexPositionsDebuggingHeader.length,
            this
        )
    }

    var drawCenter = false

    fun scale(
        x: Float,
        y: Float,
        z: Float,
        shouldNotifyThatVerticesChanged: Boolean = true,
        updateDefaultVertices: Boolean = false
    ) {
        localMatrix.scale(x, y, z)
        if (shouldNotifyThatVerticesChanged) {
            notifyObjectChanged()
        }
        if (updateDefaultVertices) {
            updateDefaultState()
        }
    }

    fun translate(
        x: Float,
        y: Float,
        z: Float,
        shouldNotifyThatObjectChanged: Boolean = true,
        updateDefaultVertices: Boolean = true
    ) {
        worldMatrix.translate(x, y, z)
        if (shouldNotifyThatObjectChanged) {
            notifyObjectChanged()
        }
        if (updateDefaultVertices) {
            updateDefaultState()
        }
    }

    fun rotate(
        degrees: Float,
        xFactor: Float,
        yFactor: Float,
        zFactor: Float,
        shouldNotifyThatObjectChanged: Boolean = true
    ) {
        Matrix4x4.multiply(
            localMatrix,
            Matrix4x4.rotate(degrees, xFactor, yFactor, zFactor),
            localMatrix
        )

        // localMatrix.rotate(degrees, xFactor, yFactor, zFactor)
        if (shouldNotifyThatObjectChanged) {
            notifyObjectChanged()
        }
    }

    // TODO fix rotations
    fun rotateEuler(
        xDegrees: Float,
        yDegrees: Float,
        zDegrees: Float,
        shouldNotifyThatObjectChanged: Boolean = true
    ) {
        Matrix4x4.multiply(
            localMatrix,
            Matrix4x4.rotateEuler(xDegrees, yDegrees, zDegrees),
            localMatrix
        )

        if (shouldNotifyThatObjectChanged) {
            notifyObjectChanged()
        }
    }

    fun rotateQuaternions(q: Vec4, shouldNotifyThatObjectChanged: Boolean = true) {
        Matrix4x4.multiply(localMatrix, Matrix4x4.fromQuaternions(q), localMatrix)

        if (shouldNotifyThatObjectChanged) {
            notifyObjectChanged()
        }
    }

    private fun drawVertexPositionsForDebugging(canvas: Canvas): Float {
        var y = 70f
        canvas.drawText(
            vertexPositionsDebuggingHeader,
            canvas.width - 10f - debugTextBounds.width(),
            y,
            debugTextPaint
        )
        val f = { x: Float -> String.format(Locale.US, "%.2f", x) }
        val i = { i: Int -> i.toString().padStart(2, ' ') }

        val drawVertexPos = { v: Vec4, y: Float, label: String ->
            val vProj = worldMatrix * localMatrix * v
            canvas.drawText(
                "$label|${f(vProj.x)}|${f(vProj.y)}|${f(vProj.z)}",
                canvas.width - 10f - debugTextBounds.width(),
                y,
                debugTextPaint
            )
        }

        for (index in 0 until min(vertices.size, 99)) {
            val v = vertices[index]
            y += debugTextBounds.height()
            drawVertexPos(v, y, i(index))
        }
        return y + debugTextBounds.height()
    }

    private fun drawMatrixForDebugging(
        canvas: Canvas,
        m: Matrix4x4,
        yOffset: Float,
        label: String
    ): Float {
        var y = yOffset
        y += debugTextBounds.height()
        canvas.drawText(
            label,
            canvas.width - 10f - debugTextBounds.width(),
            y,
            debugTextPaint
        )
        y += debugTextBounds.height()
        for (index in 0..3) {
            val row = m.getRow(index)
            canvas.drawText(
                row.joinToString(),
                canvas.width - 10f - debugTextBounds.width(),
                y,
                debugTextPaint
            )
            y += debugTextBounds.height()
        }
        return y
    }

    fun draw(
        canvas: Canvas,
        projectedPointToScreen: (p: Vec4) -> PointF,
        projectionMatrix: Matrix4x4
    ) {
        val projection = projectionMatrix * worldMatrix * localMatrix
        if (drawVertexPositionsForDebugging) {
            var y = drawVertexPositionsForDebugging(canvas)
            y = drawMatrixForDebugging(canvas, worldMatrix, y, "World matrix")
            y = drawMatrixForDebugging(canvas, localMatrix, y, "Local matrix")
        }

        drawSelf(canvas, projectedPointToScreen, projection)

        for (child in children) {
            child.draw(canvas, projectedPointToScreen, projection)
        }
    }

    protected abstract fun drawSelf(
        canvas: Canvas,
        projectedPointToScreen: (p: Vec4) -> PointF,
        projectionMatrix: Matrix4x4
    )

    fun notifyObjectChanged() {
        onObjectChanged?.onObjectChanged()
    }

    /**Updates the default state of this object 3d using it's current state*/
    fun updateDefaultState(applyOnChildren: Boolean = true) {
        for (i in vertices.indices) {
            defaultLocalMatrix.assign(localMatrix)
            defaultWorldMatrix.assign(worldMatrix)
        }
        if (applyOnChildren) {
            for (child in children) {
                child.updateDefaultState(
                    applyOnChildren = true
                )
            }
        }
    }

    fun resetToDefaultState(
        applyOnChildren: Boolean = true,
        shouldNotifyThatObjectChanged: Boolean = true
    ) {
        localMatrix.assign(defaultLocalMatrix)
        worldMatrix.assign(defaultWorldMatrix)
        if (applyOnChildren) {
            for (child in children) {
                child.resetToDefaultState(
                    applyOnChildren = true,
                    shouldNotifyThatObjectChanged = false
                )
            }
        }

        if (shouldNotifyThatObjectChanged) {
            notifyObjectChanged()
        }
    }

    companion object {
        private val debugTextPaint = Paint(0).apply {
            color = Color.WHITE
        }
    }
}
