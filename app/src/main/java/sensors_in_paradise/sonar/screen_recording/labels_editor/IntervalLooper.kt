package sensors_in_paradise.sonar.screen_recording.labels_editor

import android.os.Handler
import android.os.Looper
import java.util.*

abstract class IntervalLooper {
    private val timer: Timer = Timer()
    private var currentIntervalStartTime = 0L
    private val uiHandler = Handler(Looper.getMainLooper())
    private var loopTimerTask: TimerTask? = null

    abstract fun seekTo(ms: Long)

    fun loopInterval(msStart: Long, msEnd: Long) {
        currentIntervalStartTime = System.currentTimeMillis()
        loopTimerTask?.cancel()
        loopTimerTask = object : TimerTask() {
            override fun run() {
                val timeInInterval = System.currentTimeMillis() - currentIntervalStartTime
                if (timeInInterval + msStart > msEnd) {
                    currentIntervalStartTime = System.currentTimeMillis()
                }
                uiHandler.post {
                    seekTo(msStart + timeInInterval)
                }
            }
        }
        timer.scheduleAtFixedRate(loopTimerTask, 0L, 1000L / VisualSequenceViewHolder.FPS)
    }
    fun stopLooping() {
        loopTimerTask?.cancel()
    }
}
