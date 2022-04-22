package sensors_in_paradise.sonar.screen_recording

data class CategoryItem(
    var itemText: String,
    var nestedList: List<String>,
    var isExpanded: Boolean = false
)
