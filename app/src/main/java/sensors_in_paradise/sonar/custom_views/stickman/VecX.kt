package sensors_in_paradise.sonar.custom_views.stickman

abstract class VecX<T: VecAbstractBase>(values: Array<Float>): VecAbstractBase(values) {
    abstract fun clone(): T


    operator fun plus(a: Float): T {
        val res = clone()
        res +=a
        return res
    }
    operator fun plus(a: VecAbstractBase): T {
        val res = clone()
        res +=a
        return res
    }
    operator fun unaryMinus(): T {
        val res = clone()
        for(i in 0 until res.size()){
            res[i] = -res[i]
        }
        return res
    }

    operator fun minus(a: VecAbstractBase): T {
        val res = clone()
        res -= a
        return res
    }
    operator fun minus(a: Float): T {
        val res = clone()
        res -= a
        return res
    }
    operator fun times(a: Float): T {
        val res = clone()
        res *= a
        return res
    }
    operator fun times(a: VecAbstractBase): T {
        val res = clone()
        res *= a
        return res
    }
    operator fun div(a: VecAbstractBase): T {
        val res = clone()
        res/=a
        return res
    }
    operator fun div(a: Float): T {
        val res = clone()
        res/=a
        return res
    }
}