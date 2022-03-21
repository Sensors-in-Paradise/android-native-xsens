package sensors_in_paradise.sonar.page2

import org.json.JSONArray
import org.json.JSONObject
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

/**
Example Structure:

{"items": [
	{
		"category": "Mittag",
		"entries": ["Essen bringen", "Füttern"]
	},
	{
		"category": "Waschen",
		"entries": ["OK waschen", "Haare kämmen"]
	},
]}
 */

class CategoryItemStorage(file: File) : JSONStorage(file) {
    lateinit var items: JSONArray
    val nonDeletableItems = ArrayList<String>()
    val nonDeletableCategories = ArrayList<String>()

    private val defaultCategory = "Others"

    override fun onFileNewlyCreated() {
        json.put("items", JSONArray())
    }

    override fun onJSONInitialized() {
        items = json.getJSONArray("items")
    }

    fun addEntryIfNotAdded(entry: String, category: String = defaultCategory, deletable: Boolean = true): Boolean {
        val alreadyAdded = isEntryAdded(entry)

        if (!alreadyAdded) {
            addCategoryIfNotAdded(category)
            addEntry(entry, category)
        }
        if (!deletable) {
            nonDeletableItems.add(entry)
            nonDeletableCategories.add(category)
        }
        return !alreadyAdded
    }

    fun addCategoryIfNotAdded(category: String) {
        if (!getCategoriesAsArrayList().contains(category)) {
            addCategory(category)
        }
    }

    fun addCategory(category: String) {
        val obj = JSONObject("""{"category":"$category", "entries":[]}""")

        items.put(obj)
        save()
    }

    // Removes all entries in the category as well
    fun removeCategory(category: String) {
        for (i in 0 until items.length()) {
            if (items.getJSONObject(i).getString("category") == category) {
                items.remove(i)
                break
            }
        }
        save()
    }

    fun getItems(): MutableList<CategoryItem> {
        val itemList = mutableListOf<CategoryItem>()

        val categories = getCategoriesAsArray()
        for (category in categories) {
            val entries = getEntriesAsArrayList(category)
            val item = CategoryItem(category, entries)
            itemList.add(item)
        }

        return itemList
    }

    fun getCategoriesAsArray(): Array<String> {
        return Array(items.length()) { i -> items.getJSONObject(i).getString("category") }
    }

    fun getCategoriesAsArrayList(): ArrayList<String> {
        return getCategoriesAsArray().toCollection(ArrayList())
    }

    fun addEntry(entry: String, category: String = defaultCategory) {
        val jsonObj = findJSONObjectByCategory(category)
        assert(jsonObj != null) { "ERROR: '$category' does not exist as category." }

        jsonObj?.getJSONArray("entries")?.put(entry)
        save()
    }

    fun removeEntry(entry: String, category: String = defaultCategory) {
        val jsonObj = findJSONObjectByCategory(category)
        if (jsonObj != null) {
            val categoryEntries: JSONArray = jsonObj.getJSONArray("entries")
            for (i in 0 until categoryEntries.length()) {
                if (categoryEntries[i] == entry) {
                    categoryEntries.remove(i)
                    break
                }
            }
        }

        save()
    }

    fun getEntriesAsArray(category: String): Array<String> {
        val jsonObj = findJSONObjectByCategory(category)
        if (jsonObj != null) {
            val categoryEntries: JSONArray = jsonObj.getJSONArray("entries")
            return Array(categoryEntries.length()) { i ->
                categoryEntries[i].toString()
            }
        }

        return emptyArray()
    }

    fun getEntriesAsArrayList(category: String): ArrayList<String> {
        return getEntriesAsArray(category).toCollection(ArrayList())
    }

    fun isEntryAdded(entry: String): Boolean {
        for (i in 0 until items.length()) {
            val jsonObject = items.getJSONObject(i)
            val categoryEntries: JSONArray = jsonObject.getJSONArray("entries")

            for (i in 0 until categoryEntries.length()) {
                if (categoryEntries[i] == entry) {
                    return true
                }
            }
        }
        return false
    }

    private fun findJSONObjectByCategory(category: String): JSONObject? {
        for (i in 0 until items.length()) {
            val jsonObj = items.getJSONObject(i)
            if (jsonObj.getString("category") == category) {
                return jsonObj
            }
        }

        return null
    }
}
