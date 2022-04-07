package sensors_in_paradise.sonar.main_activity.page2

data class CategoryItem(
    var itemText: String,
    var nestedList: List<String>,
    var isExpanded: Boolean = false
)
