package sensors_in_paradise.sonar.page2

data class CategoryItem(
    var itemText: String,
    var nestedList: List<String>,
    var isExpanded: Boolean = false
)
