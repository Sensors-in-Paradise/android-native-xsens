package sensors_in_paradise.sonar.page2

data class CategoryItem(
    var itemText: String,
    var nestedList: List<Pair<String, Boolean>>,
    var isExpanded: Boolean = false
)
