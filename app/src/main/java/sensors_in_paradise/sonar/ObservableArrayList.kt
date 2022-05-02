package sensors_in_paradise.sonar

import java.util.function.Predicate

open class ObservableArrayList<T>: ArrayList<T>() {
    private val onSizeChangedListeners = ArrayList<(size: Int)-> Unit>()
    fun addOnSizeChangedListener(listener: (size: Int)-> Unit){
        onSizeChangedListeners.add(listener)
    }

    private fun notifyObserversOfSizeChanged(){
        for(listener in onSizeChangedListeners){
            listener(size)
        }
    }
    override fun add(element: T): Boolean {
        val result = super.add(element)
        notifyObserversOfSizeChanged()
        return result
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        notifyObserversOfSizeChanged()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val result = super.addAll(elements)
        notifyObserversOfSizeChanged()
        return result
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = super.addAll(index, elements)
        notifyObserversOfSizeChanged()
        return result
    }

    override fun remove(element: T): Boolean {
        val result = super.remove(element)
        notifyObserversOfSizeChanged()
        return result
    }

    override fun removeAt(index: Int): T {
        val result =super.removeAt(index)
        notifyObserversOfSizeChanged()
        return result
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val result =super.removeAll(elements)
        notifyObserversOfSizeChanged()
        return result
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        val result = super.removeIf(filter)
        notifyObserversOfSizeChanged()
        return result
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        notifyObserversOfSizeChanged()
    }

    override fun clear() {
        super.clear()
        notifyObserversOfSizeChanged()
    }
}