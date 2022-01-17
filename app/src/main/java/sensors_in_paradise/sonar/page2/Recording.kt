package sensors_in_paradise.sonar.page2

import sensors_in_paradise.sonar.GlobalValues
import java.io.File

open class Recording(val dir: File, val metadataStorage: RecordingMetadataStorage) {
    constructor(dir: File) : this(
        dir,
        RecordingMetadataStorage(dir.resolve(GlobalValues.METADATA_JSON_FILENAME))
    )
    constructor(recording:Recording) : this(
        recording.dir,
        recording.metadataStorage
    )
    val areFilesValid = !areFilesEmpty(dir)

    private fun areFilesEmpty(dir: File): Boolean {
        val emptyFileSize = 430
        val childCSVs = dir.listFiles { _, name -> name.endsWith(".csv") }
        if (childCSVs != null) {
            for (child in childCSVs) {
                if (child.length() < emptyFileSize) {
                    return true
                }
            }
        }
        return false
    }

    fun getDirectory(): File {
        return dir
    }
    fun getRecordingFiles(): Array<File>{
        return dir.listFiles{file -> file.isFile && file.endsWith(".csv")}?: emptyArray()
    }
}
