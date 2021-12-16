package sensors_in_paradise.sonar.page2

import android.content.Context
import org.json.JSONArray
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class RecordingLabelsStorage(context: Context) : JSONStorage(File(context.dataDir, "recordingLabels.json")) {
    lateinit var labels: JSONArray
    override fun onFileNewlyCreated() {
        json.put("labels", JSONArray())
    }

    override fun onJSONInitialized() {
        labels = json.getJSONArray("labels")
    }

    fun addLabel(label: String) {
        labels.put(label)
        save()
    }
    fun removeLabel(label: String) {
        for (i in 0 until labels.length()) {
            if (labels[i] == label) {
                labels.remove(i)
                break
            }
        }
       save()
    }
    fun getLabelsArray(): Array<String> {
        return Array(this.labels.length()) { i -> this.labels[i].toString() }
    } }
