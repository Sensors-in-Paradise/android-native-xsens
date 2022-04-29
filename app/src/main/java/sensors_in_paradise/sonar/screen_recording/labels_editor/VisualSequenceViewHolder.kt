package sensors_in_paradise.sonar.screen_recording.labels_editor

import sensors_in_paradise.sonar.page2.labels_editor.IntervalLooper

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
