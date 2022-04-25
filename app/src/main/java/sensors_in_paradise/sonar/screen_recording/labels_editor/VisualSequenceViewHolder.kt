package sensors_in_paradise.sonar.page2.labels_editor

import java.util.*

abstract class VisualSequenceViewHolder(protected val onPreparedListener: () -> Unit) : IntervalLooper() {
    var sourcePath: String? = null
        set(value) {
            field = value
            if (value != null) {
                loadSource(value)
            }
        }
    protected abstract fun loadSource(sourcePath: String)

    abstract override fun seekTo(ms: Long)
}
