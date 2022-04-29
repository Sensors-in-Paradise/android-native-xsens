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

class UseCasesAdapter(
    val context: Context,
    val useCases: ArrayList<UseCase>,
    val onSelectedUseCaseChanged: (useCase: UseCase) -> Unit
) :
    RecyclerView.Adapter<UseCasesAdapter.ViewHolder>() {
    private val uiHandler = Handler(Looper.getMainLooper())
    private var lastMeasuredSize = 0
    var selectedIndex = 0
        set(value) {
            val before = field
            if (field != value) {
                field = value

                uiHandler.run {
                    Log.d("UseCasesAdapter", "setting selected Index $value")
                    notifyItemChanged(before)
                    if (value >= lastMeasuredSize) {
                        notifyItemInserted(value)
                    } else {
                        notifyItemChanged(value)
                    }
                    onSelectedUseCaseChanged(useCases[value])
                }
            }
        }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val radioButton: RadioButton = view.findViewById(R.id.radioButton_useCaseItem)
        val subDirsRadioGroup: RadioGroup = view.findViewById(R.id.radioGroup_subDirs_useCaseItem)
        val subDirsLL: LinearLayout = view.findViewById(R.id.linearLayout_subdirs_useCaseItem)
        val addSubDirEditText: EditText = view.findViewById(R.id.editText_addNewSubdirectory_useCaseItem)
        val addSubDirButton: ImageButton = view.findViewById(R.id.imageButton_addNewSubdirectory_useCaseItem)
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

        val addRadioButtonToSubDirs: (name: String, id: Int, selected: Boolean) -> Unit = { name, id, selected ->
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
                    selectedIndex = position
                }
            }
        }
        holder.subDirsLL.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.subDirsRadioGroup.apply {
            removeAllViews()
            setOnCheckedChangeListener {
                _, index ->
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
                    isEnabled = value !in subDirs && len> 0
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

                    uiHandler.run {
                        notifyItemChanged(position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        lastMeasuredSize = useCases.size
        return useCases.size
    }
}