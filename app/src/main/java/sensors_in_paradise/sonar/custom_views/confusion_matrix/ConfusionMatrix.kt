package sensors_in_paradise.sonar.custom_views.confusion_matrix

class ConfusionMatrix(labels: Array<String>) {
    val labels = labels.mapIndexed { index, label ->
        label to index
    }.toMap()
    private val data = ArrayList<ArrayList<Int>>()
    private var maxCellValue = 0

    init {
        initData(labels)
    }

    private fun initData(labels: Array<String>) {
        data.clear()
        for (l in labels) {
            val column = ArrayList<Int>()
            for (i in labels) {
                column.add(0)
            }
            data.add(column)
        }
    }

    operator fun get(column: Int, row: Int): Int {
        return data[column][row]
    }

    operator fun set(column: Int, row: Int, value: Int) {
        data[column][row] = value
    }

    fun getColumn(column: Int): Collection<Int> {
        return data[column]
    }

    fun getNumLabels(): Int {
        return labels.size
    }

    fun getLabels(): Collection<String> {
        return labels.keys
    }

    fun getMaxCellValue(): Int {
        return maxCellValue
    }

    fun addPredictions(predictions: Array<String>, actualLabels: Array<String>) {
        if (predictions.size != actualLabels.size) {
            throw IllegalArgumentException("Size of predictions (${predictions.size}) must be equal to size of labels (${actualLabels.size})")
        }
        for (i in predictions.indices) {
            val prediction = predictions[i]
            val label = actualLabels[i]

            val predictionIndex = labels[prediction]
            val labelIndex = labels[label]

            if (predictionIndex == null || labelIndex == null) {
                throw IllegalArgumentException(
                    "Prediction ($prediction) or actual label " +
                            "($label) at index $i are not in specified labels this ConfusionMatrix " +
                            "was initialized with."
                )
            }
            if (++this[predictionIndex, labelIndex] > maxCellValue) {
                maxCellValue = this[predictionIndex, labelIndex]
            }
        }
    }


    companion object {
        const val COLUMN_AXIS_LABEL = "Predicted label"
        const val ROW_AXIS_LABEL = "True label"
    }

}