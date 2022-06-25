package sensors_in_paradise.sonar.custom_views.confusion_matrix

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withRotation
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R

class ConfusionMatrixView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {


    private class BoundedTextPaint(
        text: String,
        color: Int,
        textStyle: Int = Typeface.BOLD,
        textSize: Float = 40f
    ) : Paint(0) {
        val textBounds = Rect()

        init {
            typeface = Typeface.create(Typeface.DEFAULT, textStyle)
            this.textSize = textSize
            getTextBounds(text, 0, text.length, textBounds)
            this.color = color
        }
    }

    private val textPaintPerNumDigits = LinkedHashMap<Int, BoundedTextPaint>()


    private val cellPaint = Paint(0).apply {
        color = context.getColor(R.color.colorPrimary)
        alpha = 255
    }

    var confusionMatrix: ConfusionMatrix? =
        ConfusionMatrix(
            arrayOf(
                "Laufen",
                "Saugen",
                "Raufen"/*, "kaufen","Haufen", "Taufen", "Schlaufen"*/
            )
        ).apply {
            addPredictions(
                arrayOf("Laufen", "Saugen", "Raufen", "Saugen", "Raufen", "Laufen"),
                arrayOf("Laufen", "Raufen", "Laufen", "Saugen", "Raufen", "Saugen")
            )
        }
        set(value) {
            field = value
            initLabelTextPaints()
        }
    private val labelTextPaints = LinkedHashMap<String, BoundedTextPaint>()

    private fun initLabelTextPaints() {
        labelTextPaints.clear()
        val cm = confusionMatrix
        if (cm != null) {
            val labels = cm.labels
            for (label in labels.keys) {
                labelTextPaints[label] = BoundedTextPaint(label, textColor)
            }
        }
    }

    private val textColor =
        GlobalValues.getAndroidColorResource(context, android.R.attr.textColorPrimary)
    private val textColorSecondary =
        GlobalValues.getAndroidColorResource(context, android.R.attr.textColorSecondary)

    init {
        initLabelTextPaints()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cm = confusionMatrix
        if (cm != null) {
            drawConfusionMatrix(cm, Rect(200, 200, width, width), canvas)
        }
    }

    private fun drawConfusionMatrix(cm: ConfusionMatrix, boundingBox: Rect, canvas: Canvas) {
        val axisTextStyle = Typeface.ITALIC
        val generalTextBounds = BoundedTextPaint("Tegt", 0, textStyle = axisTextStyle).textBounds
        canvas.drawRect(boundingBox, Paint().apply {
            color = context.getColor(R.color.slightBackgroundContrast)
            alpha = 50
        })

        // Draw horizontal axis label
        val text = ConfusionMatrix.COLUMN_AXIS_LABEL
        val paint = BoundedTextPaint(text, textColorSecondary, textStyle = axisTextStyle)
        val textBounds = paint.textBounds
        canvas.drawText(
            text,
            boundingBox.left + ((boundingBox.width() - generalTextBounds.height()) / 2f) + generalTextBounds.height() - textBounds.width() / 2f,
            boundingBox.top + generalTextBounds.height() - generalTextBounds.bottom.toFloat(),
            paint
        )


        val contentAndLabelsBoundingBox = Rect(boundingBox).apply {
            top += generalTextBounds.height()
            left += generalTextBounds.height()
        }
        drawConfusionMatrixContentAndLabels(cm, contentAndLabelsBoundingBox, canvas)
    }

    private fun drawConfusionMatrixContentAndLabels(
        cm: ConfusionMatrix,
        boundingBox: Rect,
        canvas: Canvas
    ) {
        val numLabels = cm.getNumLabels()
        val labels = cm.getLabels()

        val generalTextBounds = BoundedTextPaint("Tegt", 0).textBounds
        val textHeight = generalTextBounds.height()
        val textBottomOffset = generalTextBounds.bottom

        val cellWidth = ((boundingBox.width() - textHeight) / numLabels).toFloat()
        val cellHeight = ((boundingBox.height() - textHeight) / numLabels).toFloat()

        canvas.drawRect(boundingBox, Paint().apply {
            color = context.getColor(R.color.slightBackgroundContrast)
            alpha = 50
        })
        val drawColumnLabel = { col: Int, label: String, paint: Paint, textWidth: Int ->
            val startX = textHeight + boundingBox.left + cellWidth * col
            val textX = startX + (cellWidth - textWidth) / 2f
            val textY = boundingBox.top + textHeight.toFloat() - textBottomOffset
            canvas.drawText(label, textX, textY, paint)
        }
        val drawRowLabel = { row: Int, label: String, paint: Paint, textWidth: Int ->
            val startY = boundingBox.top + textHeight + cellHeight * row

            val textY = startY + (cellHeight + textHeight) / 2f - textBottomOffset
            val textX = boundingBox.left - textWidth.toFloat() / 2 + textHeight / 2

            val textCenterX = textX + textWidth / 2f
            val textCenterY = textY + textBottomOffset - textHeight / 2f

            canvas.withRotation(-90f, textCenterX, textCenterY) {
                drawText(label, textX, textY, paint)
            }
        }

        for ((i, label) in labels.withIndex()) {
            val paint = labelTextPaints[label]!!
            val textBounds = paint.textBounds
            val textWidth = textBounds.width()

            drawColumnLabel(i, label, paint, textWidth)
            drawRowLabel(i, label, paint, textWidth)
        }

        val contentBoundingBox = Rect(boundingBox)
        contentBoundingBox.top += textHeight
        contentBoundingBox.left += textHeight
        drawConfusionMatrixContent(cm, contentBoundingBox, canvas)
    }

    private fun drawConfusionMatrixContent(cm: ConfusionMatrix, boundingBox: Rect, canvas: Canvas) {
        val maxCellValue = cm.getMaxCellValue()
        val numLabels = cm.getNumLabels()
        val cellWidth = (boundingBox.width() / numLabels).toFloat()
        val cellHeight = (boundingBox.height() / numLabels).toFloat()
        canvas.drawRect(boundingBox, Paint().apply {
            color = context.getColor(R.color.slightBackgroundContrast)
            alpha = 50
        })
        for (col in 0 until numLabels) {

            val x = cellWidth * col + boundingBox.left
            for (row in 0 until numLabels) {
                val y = cellHeight * row + boundingBox.top
                val cellValue = cm[col, row]
                canvas.drawRect(
                    x,
                    y,
                    x + cellWidth,
                    y + cellHeight,
                    getPaintForCell(cellValue, maxCellValue)
                )
                val textPaint = getNumberTextPaint(cellValue)
                val textBounds = textPaint.textBounds
                val cellBounds =
                    Rect(x.toInt(), y.toInt(), (x + cellWidth).toInt(), (y + cellHeight).toInt())

                val textX = cellBounds.exactCenterX() - textBounds.width() / 2
                val textY = cellBounds.exactCenterY() + textBounds.height() / 2
                canvas.drawText(cellValue.toString(), textX, textY, textPaint)
            }
        }
    }

    private fun getPaintForCell(cellValue: Int, maxCellValue: Int): Paint {
        val relativeShare = (cellValue * 255) / maxCellValue
        cellPaint.alpha = relativeShare
        return cellPaint
    }

    private fun getNumberTextPaint(textValue: Int): BoundedTextPaint {
        val text = textValue.toString()
        val textPaint = textPaintPerNumDigits[text.length]
        if (textPaint != null) {
            return textPaint
        }
        val newTextPaint = BoundedTextPaint(text, textColor)
        textPaintPerNumDigits[text.length] = newTextPaint
        return newTextPaint
    }
}
