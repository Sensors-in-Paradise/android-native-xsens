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
) constructor(val file: File) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    lateinit var json: JSONObject

    init {
        if (!file.exists()) {
            file.createNewFile()
            json = JSONObject()
            onFileNewlyCreated()
            save()
        } else {
            val fileContentRaw = Files.readAllBytes(file.toPath())
            val fileContent = String(fileContentRaw, StandardCharsets.UTF_8)
            json = JSONObject(fileContent)
        }
        onJSONInitialized()
    }

    @Throws(IOException::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun save() {
        Files.write(file.toPath(), json.toString().encodeToByteArray())
    }

    /** Initialize the json object with all its members here.
     * Changes to the json object will automatically saved afterwards*/
    abstract fun onFileNewlyCreated()

    /** */
    abstract fun onJSONInitialized()
}
