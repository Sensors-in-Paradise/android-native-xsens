package sensors_in_paradise.sonar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.view.postDelayed

class SensorDataTrafficIndicator(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private class TrafficIndicator(var  rect: Rect,var paint: Paint){
        var lastDataReceived = 0L
    }
    private val defaultNumSensors = 1
    var numSensors = defaultNumSensors
        set(value) {
            field = if(value!=0) value else 1
            initTrafficIndicators()
            invalidate()
        }
    private val defaultPixelsBetween = 12
    var pixelsBetween = defaultPixelsBetween
        set(value) {
            field = value
            invalidate()
        }
    private val defaultIndicatorColor = Color.RED
    private var indicatorColor = defaultIndicatorColor
        set(value) {
            field = value
            invalidate()
        }
    private val backgroundPaint = Paint(0).apply {
        color = Color.BLACK
    }
    private var trafficIndicators: Array<TrafficIndicator>? = null
    private var animationRunning = false
    private val defaultFadeOutDuration = 1000
    private var fadeOutDuration = defaultFadeOutDuration
        set(value) {
            field = value
            invalidate()
        }
    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SensorDataTrafficIndicator,
            0, 0
        ).apply {

            try {
                indicatorColor = getColor(
                    R.styleable.SensorDataTrafficIndicator_indicatorColor,
                    defaultIndicatorColor
                )
                pixelsBetween = getInt(
                    R.styleable.SensorDataTrafficIndicator_pixelsBetween,
                    defaultPixelsBetween
                )
                fadeOutDuration = getInt(
                    R.styleable.SensorDataTrafficIndicator_fadeOutDuration,
                    defaultFadeOutDuration
                )
            } finally {
                recycle()
            }
        }
    }

private fun initTrafficIndicators(){
    val w = width
    val h = height
    val widthPerSensor = ((w - paddingLeft - paddingRight) / numSensors)


    trafficIndicators = Array(numSensors) { i ->
        TrafficIndicator(Rect(
            paddingLeft + i * widthPerSensor + if (i != 0) pixelsBetween / 2 else 0,
            paddingTop,
            paddingLeft + (i + 1) * widthPerSensor - if (i != numSensors - 1) pixelsBetween / 2 else 0,
            h - paddingBottom
        ), Paint(0).apply {
            color = indicatorColor
            alpha=0
        })
    }
}
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        initTrafficIndicators()

        doAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            //drawRect(clipBounds, backgroundPaint)
            trafficIndicators?.forEach {
                drawRect(it.rect, it.paint)
            }
        }
    }

    fun setSensorDataReceived(sensorIndex: Int) {
        if(trafficIndicators!=null){
            if(sensorIndex< trafficIndicators!!.size){
                trafficIndicators!![sensorIndex].lastDataReceived = System.currentTimeMillis()
            }
        }
        if (!animationRunning) {
            doAnimation()
        }
    }

    /** Returns true if there is one fading out animation still running, false otherwise*/
    private fun updatePaints(): Boolean {
        val currTime = System.currentTimeMillis()
        var result = false
        if (trafficIndicators != null) {
            for (indicator in trafficIndicators!!) {
                val lastReceived = indicator.lastDataReceived
                val paint = indicator.paint

                val timeDiff = currTime - lastReceived


                if (timeDiff <= fadeOutDuration) {
                    paint.alpha = 255 - ((timeDiff * 255) / fadeOutDuration).toInt()
                    paint.color = indicatorColor
                    result = true
                } else {
                    paint.alpha = 255
                    paint.color  = Color.GRAY
                }
            }
        }
        return result
    }

    private fun doAnimation() {
        animationRunning = updatePaints()
        invalidate()
        if (animationRunning) {
            postDelayed(40) {
                doAnimation()
            }
        }
    }
}