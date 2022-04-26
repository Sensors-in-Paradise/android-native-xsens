package sensors_in_paradise.sonar.page2.labels_editor

abstract class VisualSequenceViewHolder(
    private val onSourceLoadedListener: () -> Unit,
    protected val onStartLoadingSource: () -> Unit
) : IntervalLooper() {
    var sourcePath: String? = null
    var isSourceLoaded = false
    fun loadSource() {
        if (sourcePath != null) {
            onStartLoadingSource()
            loadSource(sourcePath!!) {
                isSourceLoaded = true
                onSourceLoadedListener()
            }
        }
    }

    protected abstract fun loadSource(sourcePath: String, onSourceLoadedListener: () -> Unit)

    abstract override fun seekTo(ms: Long)
}
