package sensors_in_paradise.sonar.page3

class Prediction(var title: String, var percentage: Float) {
    fun percentageAsString(): String {
        return "$percentage%"
    }

    // Equivalent to Java's static keyword
    companion object {
        val PredictionsComparator =  Comparator<Prediction> { left, right ->
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            when {
                (left.percentage < right.percentage) -> 0
                (left.percentage > right.percentage) -> -1
                else -> 0
            }
        }
    }
}
