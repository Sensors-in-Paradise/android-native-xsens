package sensors_in_paradise.sonar.main_activity.page2.labelling

data class CategoryItem(
    var itemText: String,
    var nestedList: List<String>,
    var isExpanded: Boolean = false
)
