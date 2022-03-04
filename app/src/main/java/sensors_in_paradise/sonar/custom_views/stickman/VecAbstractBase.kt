package sensors_in_paradise.sonar.custom_views.stickman

abstract class VecAbstractBase(private val values: Array<Float>) {
    constructor(size: Int) : this(Array(size) { _ -> 0f })


    fun size():Int {
        return values.size
    }
    operator fun get(index: Int): Float {
        return values[index]
    }
    operator fun set(index: Int, value: Float) {
        values[index] = value
    }
    override operator fun equals(a: Any?): Boolean{
        if (a is VecAbstractBase && a.size() == this.size()){
            for((i, e) in values.withIndex()){
                if(e != a.values[i]){
                    return false
                }
            }
            return  true
        }
        return false
    }

    operator fun plusAssign(a: Float) {
        for(i in 0 until size()){
             values[i] += a
        }
    }
    operator fun plusAssign(a: VecAbstractBase) {
        assert(a.size()<=size())
        for(i in 0 until a.size()){
            values[i] += a[i]
        }
    }

    operator fun minusAssign(a: Float) {
        this += -a
    }
    operator fun minusAssign(a: VecAbstractBase) {
        assert(a.size()<=size())
        for(i in 0 until a.size()){
            values[i] -= a[i]
        }
    }

    operator fun timesAssign(a: Float) {
        for(i in 0 until size()){
            values[i] *= a
        }
    }
    operator fun timesAssign(a: VecAbstractBase) {
        assert(a.size()<=size())
        for(i in 0 until a.size()){
            values[i] *= a[i]
        }
    }

    operator fun divAssign(a: Float) {
        for(i in 0 until size()){
            values[i] /= a
        }
    }
    operator fun divAssign(a: VecAbstractBase) {
        assert(a.size()<=size())
        for(i in 0 until a.size()){
            values[i] /= a[i]
        }
    }

}