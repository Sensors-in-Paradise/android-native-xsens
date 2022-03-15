package sensors_in_paradise.sonar.page2

import org.json.JSONArray
import org.json.JSONObject
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

// TODO: Others as default category


/*
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

    override fun onFileNewlyCreated() {
        json.put("items", JSONArray())
    }

    override fun onJSONInitialized() {
        items = json.getJSONArray("items")
    }

    fun addItemIfNotAdded(item: String, deletable: Boolean = true): Boolean {
        var alreadyAdded = false
        for (i in 0 until items.length()) {
            if (items[i] == item) {
                alreadyAdded = true
                break
            }
        }
        if (!alreadyAdded) {
            addEntry(item)
            save()
        }
        if (!deletable) {
            nonDeletableItems.add(item)
        }
        return !alreadyAdded
    }

    fun addCategory(category: String) {
        val obj = JSONObject("""{"category":"$category", "entries":[]}""")
        items.put(obj)
        save()
    }

    fun removeCategory(category: String) {
        for (i in 0 until items.length()) {
            if (items.getJSONObject(i).getString("category") == category) {
                items.remove(i)
                break
            }
        }
        save()
    }

    fun getCategoriesAsArray(): Array<String> {

    }

    fun getCategoriesAsArrayList(): ArrayList<String> {

    }

    // TODO: Handle if no matching category found
    fun addEntry(entry: String, category: String = "Others") {
        for (i in 0 until items.length()) {
            var jsonObj = items.getJSONObject(i)
            if (jsonObj.getString("category") == category) {
                jsonObj.getJSONArray("entries").put(entry)
                break
            }
        }
        save()
    }

    fun removeEntry(entry: String, category: String = "Others") {
        for (i in 0 until items.length()) {
            var jsonObj = items.getJSONObject(i)
            if (jsonObj.getString("category") == category) {
                jsonObj.getJSONArray("entries").remove(entry)
                break
            }
        }
        save()
    }

    fun getEntriesAsArray(): Array<String> {
        return Array(this.items.length()) { i -> this.items[i].toString() }
    }

    fun getEntriesAsArrayList(): ArrayList<String> {
        return getEntriesAsArray().toCollection(ArrayList())
    }
}