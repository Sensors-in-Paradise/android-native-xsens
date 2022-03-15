package sensors_in_paradise.sonar.page2

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R
import java.io.File
import java.util.*

class PersistentStringArrayDialog(
    val context: Context,
    title: String,
    storageFile: File,
    cancellable: Boolean = true,
    defaultItem: String,
    callback: (value: String) -> Unit = {},
) {
    var dialog: AlertDialog
//    private val storage = StringItemStorage(storageFile)
    private val storage = CategoryItemStorage(storageFile)
    var adapter: PersistentCategoriesAdapter
//    var adapter: PersistentStringArrayAdapter

    init {
        storage.addEntryIfNotAdded(defaultItem, deletable = false)

//        storage.addItemIfNotAdded(defaultItem, false)
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)

        val root = LayoutInflater.from(context).inflate(R.layout.string_array_dialog, null)
        val searchEditText = root.findViewById<EditText>(R.id.editText_search_stringArrayDialog)
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView_items_stringArrayDialog)

        adapter = PersistentCategoriesAdapter(storage)
        rv.adapter = adapter
        builder.setView(root)
        builder.setPositiveButton("Add new item", null)

//        adapter = PersistentStringArrayAdapter(storage)
//        rv.adapter = adapter
//        builder.setView(root)
//        builder.setPositiveButton("Add new item", null)

        dialog = builder.create()
//        adapter.setOnItemClickedListener { value ->
//            callback(value)
//            dialog.dismiss()
//        }

        dialog.setOnShowListener {
            dialog.setCancelable(cancellable)
            val button: Button =
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = false
            button.setOnClickListener {
//                storage.addItem(searchEditText.text.toString().lowercase(Locale.getDefault()))
                storage.addEntry(searchEditText.text.toString().lowercase(Locale.getDefault()))
//                adapter.notifyItemInserted(storage.items.length() - 1)
                adapter.notifyDataSetChanged()
                searchEditText.setText("")
            }
//            searchEditText.addTextChangedListener { text ->
//                button.isEnabled = (!isItemAlreadyAdded(text.toString()) && text.toString() != "")
//                adapter.filter(text.toString())
//            }
        }

        dialog.show()
    }
//    private fun isItemAlreadyAdded(item: String): Boolean {
//        val currentLabels = storage.getItemsAsArray()
//        return currentLabels.contains(item)
//    }
//
//    fun setCancelListener(listener: DialogInterface.OnCancelListener) {
//        dialog.setOnCancelListener(listener)
//    }
}
