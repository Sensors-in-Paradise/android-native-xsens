package sensors_in_paradise.sonar

import android.content.Context
import java.io.File

class XSensDotMetadataStorage(context: Context) : JSONStorage(File(context.dataDir, "xsensDotMetadata.json")) {
    override fun onFileNewlyCreated() {
    }

    override fun onJSONInitialized() {
    }

    fun setTagForAddress(address: String, name: String) {
        json.put(address, name)
        save()
    }
    fun getTagForAddress(address: String): String {
        return json.optString(address)
    }
}
