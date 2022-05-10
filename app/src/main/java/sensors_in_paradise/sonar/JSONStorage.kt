package sensors_in_paradise.sonar

import androidx.annotation.VisibleForTesting
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

abstract class JSONStorage @Throws(
    IOException::class,
    SecurityException::class,
    JSONException::class
) constructor(val file: File?, initialJson: JSONObject? = null) {
    constructor(file: File?) : this(file, null)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var json: JSONObject

    init {
        if (initialJson == null) {
            if (file?.exists() != true) {
                file?.createNewFile()
                json = JSONObject()
                onFileNewlyCreated()
                save()
            } else {
                val fileContentRaw = Files.readAllBytes(file.toPath())
                val fileContent = String(fileContentRaw, StandardCharsets.UTF_8)
                json = JSONObject(fileContent)
            }
        } else {
            val jsonCopy = JSONObject(initialJson.toString())
            if (file?.exists() != true) {
                file?.createNewFile()
                json = jsonCopy
                onFileNewlyCreated()
                save()
            } else {
                json = jsonCopy
                save()
            }
        }
        onJSONInitialized()
    }

    @Throws(IOException::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun save() {
        file?.let { Files.write(file.toPath(), json.toString().encodeToByteArray()) }
    }

    /** Initialize the json object with all its members here.
     * Changes to the json object will automatically saved afterwards*/
    abstract fun onFileNewlyCreated()

    /** */
    abstract fun onJSONInitialized()

    fun getJsonString(indentSpaces: Int = 4): String {
        return json.toString(indentSpaces)
    }
}
