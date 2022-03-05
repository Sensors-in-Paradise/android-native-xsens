package sensors_in_paradise.sonar.custom_views.stickman.math

abstract class VecAbstractBase(protected val values: FloatArray,  size: Int) {

    var size = size
        protected set(value) {
            if(value<=values.size){
                field = value
            }
            else{
                throw Exception("Can't set size of vector bigger than underlying array. Trying to set size to $value while array has size ${values.size}")
            }
        }

    operator fun get(index: Int): Float {
        return values[index]
    }
    operator fun set(index: Int, value: Float) {
        values[index] = value
    }
    override operator fun equals(other: Any?): Boolean{
        if (other is VecAbstractBase && other.size == this.size){
            for((i, e) in values.withIndex()){
                if(e != other.values[i]){
                    return false
                }
            }
            return  true
        }
        return false
    }

    operator fun plusAssign(a: Float) {
        for(i in 0 until size){
             values[i] += a
        }
    }
    operator fun plusAssign(a: VecAbstractBase) {
        assert(a.size<=size)
        for(i in 0 until a.size){
            values[i] += a[i]
        }
    }

    operator fun minusAssign(a: Float) {
        this += -a
    }
    operator fun minusAssign(a: VecAbstractBase) {
        assert(a.size<=size)
        for(i in 0 until a.size){
            values[i] -= a[i]
        }
    }

    operator fun timesAssign(a: Float) {
        for(i in 0 until size){
            values[i] *= a
        }
    }
    operator fun timesAssign(a: VecAbstractBase) {
        assert(a.size<=size)
        for(i in 0 until a.size){
            values[i] *= a[i]
        }
    }

    operator fun divAssign(a: Float) {
        for(i in 0 until size){
            values[i] /= a
        }
    }
    operator fun divAssign(a: VecAbstractBase) {
        assert(a.size<=size)
        for(i in 0 until a.size){
            values[i] /= a[i]
        }
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + size
        return result
    }

}