package sensors_in_paradise.sonar.screen_recording.labels_editor

import android.os.Handler
import android.os.Looper
import java.util.*

abstract class IntervalLooper {
    private val timer: Timer = Timer()
    private var currentIntervalStartTime = 0L
    private val uiHandler = Handler(Looper.getMainLooper())
    private var loopTimerTask: TimerTask? = null
    private var currentInterval: Pair<Long, Long>? = null
    private var timeInInterval = 0L
    abstract fun seekTo(ms: Long)

    fun loopInterval(msStart: Long, msEnd: Long, relativeStartPosition: Long = 0L) {
        currentInterval = Pair(msStart, msEnd)
        currentIntervalStartTime = System.currentTimeMillis() - relativeStartPosition
        loopTimerTask?.cancel()
        loopTimerTask = object : TimerTask() {
            override fun run() {
                timeInInterval = System.currentTimeMillis() - currentIntervalStartTime
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

    fun resumeLooping(relativeStartPosition: Long = timeInInterval): Boolean {
        if (currentInterval == null) {
            return false
        }
        loopInterval(currentInterval!!.first, currentInterval!!.second, relativeStartPosition)
        return true
    }
}
