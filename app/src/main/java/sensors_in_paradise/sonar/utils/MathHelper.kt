package sensors_in_paradise.sonar.utils

import com.xsens.dataprocessor.XsQuaternion
import com.xsens.dataprocessor.XsVector
import java.nio.ByteBuffer

class MathHelper {
    fun getCalFreeAcc(localGravity: Double, quat: ArrayList<Float>?, acc: ArrayList<Float>?): ByteArray? {
        val freeAcc = FloatArray(3)
        if (quat != null && acc != null) {
            val q = XsQuaternion()
            q.assign(
                quat[0].toDouble(),
                quat[1].toDouble(),
                quat[2].toDouble(),
                quat[3].toDouble()
            )
            var qv = XsQuaternion()
            qv.assign(0.0, acc[0].toDouble(), acc[1].toDouble(), acc[2].toDouble())
            val qi = q.inverse()
            qv = multiply(qv, qi)
            qv = multiply(q, qv)
            val accL = XsVector(3L)
            accL.setValue(0L, qv.x())
            accL.setValue(1L, qv.y())
            accL.setValue(2L, qv.z())
            freeAcc[0] = accL.value(0L).toFloat()
            freeAcc[1] = accL.value(1L).toFloat()
            freeAcc[2] = (accL.value(2L) - localGravity).toFloat()
        }
        return  floatArray2ByteArray(freeAcc)
    }

    private fun floatArray2ByteArray(values: FloatArray): ByteArray? {
        val buffer = ByteBuffer.allocate(4 * values.size)
        for (value in values) {
            buffer.putFloat(value)
        }
        return buffer.array()
    }

    private fun multiply(a: XsQuaternion, b: XsQuaternion): XsQuaternion {
        val qa0 = a.w()
        val qa1 = a.x()
        val qa2 = a.y()
        val qa3 = a.z()
        val qb0 = b.w()
        val qb1 = b.x()
        val qb2 = b.y()
        val qb3 = b.z()
        val q = XsQuaternion()
        q.assign(
            qa0 * qb0 - qa1 * qb1 - qa2 * qb2 - qa3 * qb3,
            qa1 * qb0 + qa0 * qb1 - qa3 * qb2 + qa2 * qb3,
            qa2 * qb0 + qa3 * qb1 + qa0 * qb2 - qa1 * qb3,
            qa3 * qb0 - qa2 * qb1 + qa1 * qb2 + qa0 * qb3
        )
        return q
    }
}