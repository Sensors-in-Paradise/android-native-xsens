package sensors_in_paradise.sonar.page2

import org.json.JSONArray
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class StringItemStorage(file: File) : JSONStorage(file) {
    lateinit var items: JSONArray
    override fun onFileNewlyCreated() {
        json.put("items", JSONArray())
    }

    override fun onJSONInitialized() {
        items = json.getJSONArray("items")
    }

    fun addItem(item: String) {
        items.put(item)
        save()
    }

    fun removeItem(item: String) {
        for (i in 0 until items.length()) {
            if (items[i] == item) {
                items.remove(i)
                break
            }
        }
       save()
    }

    fun getItemsAsArray(): Array<String> {
        return Array(this.items.length()) { i -> this.items[i].toString() }
    }
    fun getItemsAsArrayList(): ArrayList<String> {
        return getItemsAsArray().toCollection(ArrayList())
    }
}
