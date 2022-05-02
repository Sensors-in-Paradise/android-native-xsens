package sensors_in_paradise.sonar.util.use_cases

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.util.dialogs.MessageDialog
import sensors_in_paradise.sonar.util.dialogs.TextInputDialog

class UseCasesAdapter(
    val context: Context,
    private val useCases: ArrayList<UseCase>,
    selectedUseCase: UseCase,
    val onSelectedUseCaseChanged: (useCase: UseCase?) -> Unit
) :
    RecyclerView.Adapter<UseCasesAdapter.ViewHolder>() {
    private val uiHandler = Handler(Looper.getMainLooper())
    private var lastMeasuredSize = 0

    private var selectedIndex = useCases.indexOf(selectedUseCase)
        set(value) {
            field = value
            onSelectedUseCaseChanged(if (field != -1) useCases[selectedIndex] else null)
        }

    fun selectItem(index: Int) {
        uiHandler.run {
            val before = selectedIndex
            if (before != index) {
                selectedIndex = index
                notifyItemChanged(before)
                if (index >= lastMeasuredSize) {
                    notifyItemInserted(index)
                } else {
                    notifyItemChanged(index)
                }
            }
        }
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioButton: RadioButton = view.findViewById(R.id.radioButton_useCaseItem)
        val subDirsRadioGroup: RadioGroup = view.findViewById(R.id.radioGroup_subDirs_useCaseItem)
        val subDirsLL: LinearLayout = view.findViewById(R.id.linearLayout_subdirs_useCaseItem)
        val addSubDirEditText: EditText =
            view.findViewById(R.id.editText_addNewSubdirectory_useCaseItem)
        val addSubDirButton: ImageButton =
            view.findViewById(R.id.imageButton_addNewSubdirectory_useCaseItem)
        val deleteButton: ImageButton = view.findViewById(R.id.imageButton_delete_useCaseItem)
        val duplicateButton: ImageButton = view.findViewById(R.id.imageButton_duplicate_useCaseItem)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.usecase_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val useCase = useCases[position]
        val subDirs = useCase.getAvailableRecordingSubDirs()
        val isSelected = selectedIndex == position

        val addRadioButtonToSubDirs: (name: String, id: Int, selected: Boolean) -> Unit =
            { name, id, selected ->
                val rb = RadioButton(context)
                rb.id = id
                rb.text = name
                rb.isChecked = selected
                holder.subDirsRadioGroup.addView(rb)
            }

        holder.radioButton.apply {
            isChecked = isSelected
            text = useCase.title
            setOnCheckedChangeListener { _, checked ->
                Log.d("UseCaseAdapter", "onCheckedChangeListener now at index $position")
                // remove on checked change listener to prevent illegal state
                if (checked) {
                    setOnCheckedChangeListener(null)
                    selectItem(indexOf(useCase))
                }
            }
        }
        holder.subDirsLL.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.subDirsRadioGroup.apply {
            removeAllViews()
            setOnCheckedChangeListener { _, index ->
                useCase.setRecordingsSubDir(subDirs[index])
            }
            for ((i, subDir) in subDirs.withIndex()) {
                addRadioButtonToSubDirs(subDir, i, useCase.getRecordingsSubDir().name == subDir)
            }
        }
        holder.addSubDirEditText.apply {

            addTextChangedListener {
                val value = text.toString()
                val len = value.length
                holder.addSubDirButton.apply {
                    isEnabled = value !in subDirs && len > 0
                    Log.d("UseCaseAdapter", "addSubDirButton isEnabled: $isEnabled")
                }
            }
        }
        holder.addSubDirButton.apply {
            isEnabled = false
            setOnClickListener {
                Log.d("UseCasesAdapter", "onClick addSubDirButton")
                holder.addSubDirEditText.apply {
                    val value = text.toString()
                    useCase.setRecordingsSubDir(value)
                    setText("")
                    uiHandler.run {
                        notifyItemChanged(indexOf(useCase))
                    }
                }
            }
        }
        holder.deleteButton.apply {
            visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            isEnabled = useCase.title != UseCaseHandler.DEFAULT_USE_CASE_TITLE
            setOnClickListener {

                MessageDialog(
                    context,
                    "Do you really want to delete the use case \"${useCase.title}\"?",
                    onPositiveButtonClickListener = { _, _ ->
                        val useCaseIndex = indexOf(useCase)
                        uiHandler.post {
                            if (selectedIndex == useCaseIndex) {
                                selectedIndex = -1
                            }
                            useCase.delete()
                            useCases.remove(useCase)
                            notifyItemRemoved(useCaseIndex)
                        }
                    })
            }
        }
        holder.duplicateButton.apply {
            visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            setOnClickListener {
                TextInputDialog(
                    context,
                    "Set title of use case",
                    { s ->
                        val copy = useCase.duplicate(s)
                        val index = indexOf(useCase) + 1
                        Log.d("UseCasesAdapter", "Placing duplicated item at $index")
                        useCases.add(index, copy)
                        uiHandler.run { notifyItemInserted(index) }
                    },
                    "Title of use case",
                    "Title",
                    useCase.title,
                    acceptanceInterface = { s ->
                        Pair(s.isNotEmpty() && useCases.find { it.title == s } == null,
                            "Title empty or does already exist")
                    })
            }
        }
    }

    private fun indexOfDefault(): Int {
        return useCases.indexOfFirst { it.title == UseCaseHandler.DEFAULT_USE_CASE_TITLE }
    }

    private fun indexOf(useCase: UseCase): Int {
        return useCases.indexOf(useCase)
    }

    override fun getItemCount(): Int {
        Log.d("UseCasesAdapter", "Measuring size of usecases ${useCases.size}")
        lastMeasuredSize = useCases.size
        return useCases.size
    }
}
