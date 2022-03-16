package sensors_in_paradise.sonar.page2

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R
import java.io.File
import java.util.*
import kotlin.collections.LinkedHashMap
import androidx.core.widget.addTextChangedListener
import sensors_in_paradise.sonar.GlobalValues

class PersistentCategoriesDialog(
    val context: Context,
    title: String,
    storageFile: File,
    cancellable: Boolean = true,
    defaultItems: LinkedHashMap<String, String>,
    callback: (value: String) -> Unit = {},
) {
    var dialog: AlertDialog
    private val storage = CategoryItemStorage(storageFile)
    var adapter: PersistentCategoriesAdapter

    init {
        // Add default categories and entries
        for ((entry, category) in defaultItems) {
            storage.addEntryIfNotAdded(entry.lowercase(Locale.getDefault()), category, deletable = false)
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val root = LayoutInflater.from(context).inflate(R.layout.string_array_dialog, null)
        val searchEditText = root.findViewById<EditText>(R.id.editText_search_stringArrayDialog)
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView_items_stringArrayDialog)

        adapter = PersistentCategoriesAdapter(storage)
        rv.adapter = adapter
        builder.setView(root)
        builder.setPositiveButton("Add new item", null)

        dialog = builder.create()
        adapter.setOnItemClickedListener { value ->
            callback(value)
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            dialog.setCancelable(cancellable)
            val button: Button =
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = false
            button.setOnClickListener {
                storage.addEntry(searchEditText.text.toString().lowercase(Locale.getDefault()))
                adapter.updateByCategory(GlobalValues.OTHERS_CATEGORY)
                searchEditText.setText("")
            }
            searchEditText.addTextChangedListener { text ->
                button.isEnabled = (!isItemAlreadyAdded(text.toString()) && text.toString() != "")
//                adapter.filter(text.toString())
            }
        }

        dialog.show()
    }
    private fun isItemAlreadyAdded(entry: String): Boolean {
        return storage.isEntryAdded(entry)
    }
}

