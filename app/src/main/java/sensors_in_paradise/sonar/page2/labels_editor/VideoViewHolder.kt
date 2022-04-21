package sensors_in_paradise.sonar.page2.labels_editor

import android.media.MediaPlayer
import android.widget.VideoView

class VideoViewHolder(private val videoView: VideoView, onPreparedListener: () -> Unit) :
    VisualSequenceViewHolder(onPreparedListener) {
    private var mediaPlayer: MediaPlayer? = null

    override fun loadSource(sourcePath: String) {
        videoView.setOnPreparedListener { mp ->
            mediaPlayer = mp
            onPreparedListener()
        }
        videoView.setVideoPath(sourcePath)
    }

    override fun seekTo(ms: Long) {
        try {
            mediaPlayer?.seekTo(ms, MediaPlayer.SEEK_CLOSEST)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val FPS = 60L
    }
}
