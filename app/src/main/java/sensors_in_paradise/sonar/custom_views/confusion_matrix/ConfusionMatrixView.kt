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


    private class BoundedTextPaint(text: String, color: Int) : Paint(0) {
        val textBounds = Rect()

        init {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            this.textSize = 40f
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
        ConfusionMatrix(arrayOf("Laufen", "Saufen", "Raufen", "kaufen","Haufen", "Taufen", "Schlaufen")).apply {
            addPredictions(
                arrayOf("Laufen", "Saufen", "Raufen", "Saufen", "Raufen", "Laufen"),
                arrayOf("Laufen", "Saufen", "Taufen", "Saufen", "Raufen", "Saufen"))
        }
     set(value) {
         field = value
         initLabelTextPaints()
     }
    private val labelTextPaints = LinkedHashMap<String, BoundedTextPaint>()

    private fun initLabelTextPaints(){
        labelTextPaints.clear()
        val cm = confusionMatrix
        if(cm!=null){
            val labels = cm.labels
            for(label in labels.keys){
                labelTextPaints[label] = BoundedTextPaint(label, textColor)
            }
        }
    }

    private val textColor = GlobalValues.getAndroidColorResource(context, android.R.attr.textColorPrimary)



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
            drawConfusionMatrix(cm, Rect(20,20,width, width), canvas)
        }
    }

    private fun drawConfusionMatrix(cm: ConfusionMatrix, boundingBox: Rect, canvas: Canvas){
        val numLabels = cm.getNumLabels()
        val labels = cm.getLabels()
        val textHeight = BoundedTextPaint("Test", 0).textBounds.height()
        val cellWidth = ((boundingBox.width()-textHeight) / numLabels).toFloat()
        val cellHeight = ((boundingBox.height()-textHeight) / numLabels).toFloat()


        for ((col, label) in labels.withIndex()) {
            val paint = labelTextPaints[label]!!
            val bounds = paint.textBounds

            val startX = textHeight+boundingBox.left + cellWidth * col

            val textX = startX + (cellWidth - bounds.width())/2f
            val textY = boundingBox.top+bounds.height().toFloat()

            canvas.drawText(label, textX, textY, paint)
        }

        for ((row, label) in labels.withIndex()) {
            val paint = labelTextPaints[label]!!
            val bounds = paint.textBounds

            val startY = boundingBox.top +textHeight+ cellHeight * row

            val textY = startY + (cellHeight+bounds.height())/2f
            val textX = boundingBox.left-bounds.width().toFloat()/2

            val textCenterX = textX + bounds.width()/2f
            val textCenterY = textY - bounds.height()/2f

            canvas.withRotation(-90f,textCenterX, textCenterY) {
                drawText(label, textX, textY, paint)
            }
        }
        val contentBoundingBox = Rect(boundingBox)
        contentBoundingBox.top+= textHeight + 5
        contentBoundingBox.left+= textHeight + 5
        drawConfusionMatrixContent(cm, contentBoundingBox, canvas)
    }

    private fun drawConfusionMatrixContent(cm: ConfusionMatrix, boundingBox: Rect, canvas: Canvas){
        val maxCellValue = cm.getMaxCellValue()
        val numLabels = cm.getNumLabels()
        val cellWidth = (boundingBox.width() / numLabels).toFloat()
        val cellHeight = (boundingBox.height() / numLabels).toFloat()

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
                val cellBounds = Rect(x.toInt(), y.toInt(), (x+cellWidth).toInt(), (y+cellHeight).toInt())

                val textX = cellBounds.exactCenterX() - textBounds.width()/2
                val textY = cellBounds.exactCenterY() + textBounds.height()/2
                canvas.drawText(cellValue.toString(), textX,textY,textPaint)
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
        if(textPaint != null){
            return textPaint
        }
        val newTextPaint = BoundedTextPaint(text, textColor)
        textPaintPerNumDigits[text.length] = newTextPaint
        return newTextPaint
    }
}
