package sensors_in_paradise.sonar.page2.camera.pose_estimation

import sensors_in_paradise.sonar.page2.LoggingManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.BodyPart
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime

class StorageManager(val outputFile: File) {
    private val separator = ", "
    private val fileWriter = FileWriter(outputFile)

    fun writeHeader(startTime: LocalDateTime, modelType: String, dimensions: Int) {
        fileWriter.appendLine("sep=$separator")
        fileWriter.appendLine("ModelType: $modelType")
        fileWriter.appendLine("Dimensions: $dimensions")
        fileWriter.appendLine("StartTime: $startTime")
        fileWriter.appendLine("")
        val columns = BodyPart.values().joinToString(
            ", ",
            "TimeStamp, ",
            transform = { bp -> "${bp}_X, ${bp}_Y" })
        fileWriter.appendLine(columns)
    }

    fun storePoses(persons: List<Person>) {
        val keyPoints = persons.getOrNull(0)?.keyPoints
        if (keyPoints != null) {
            val timeStamp = LoggingManager.normalizeTimeStamp(LocalDateTime.now())
            val outputLine = keyPoints.joinToString(
                ", ",
                "$timeStamp, ",
                transform = { kp -> "${kp.coordinate.x}, ${kp.coordinate.y}" })
            fileWriter.appendLine(outputLine)
        }
    }
}
