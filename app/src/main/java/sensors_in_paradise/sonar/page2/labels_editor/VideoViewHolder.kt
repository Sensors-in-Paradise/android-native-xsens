package sensors_in_paradise.sonar.page2.labels_editor

import android.media.MediaPlayer
import android.util.Log
import android.widget.VideoView

class VideoViewHolder(
    private val videoView: VideoView,
    onSourceLoadedListener: () -> Unit,
    onStartLoadingSource: () -> Unit
) :
    VisualSequenceViewHolder(onSourceLoadedListener, onStartLoadingSource) {
    private var mediaPlayer: MediaPlayer? = null

    override fun loadSource(sourcePath: String, onSourceLoadedListener: () -> Unit) {
        Log.d("LabelsEditorDialog-VideoViewHolder", "loadSource $sourcePath")
        videoView.setOnPreparedListener { mp ->
            mediaPlayer = mp
            Log.d("LabelsEditorDialog-VideoViewHolder", "videoView.setOnPreparedListener ")
            onSourceLoadedListener()
        }
        videoView.setVideoPath(sourcePath)
    }

    override fun seekTo(ms: Long) {
        try {
            mediaPlayer?.seekTo(ms, MediaPlayer.SEEK_CLOSEST)
        } catch (e: IllegalStateException) {
            e.message?.let { Log.e("VideoViewHolder", it) }
        }
    }
}
