package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import android.content.Context
import android.graphics.PointF
import android.widget.Toast
import java.time.LocalDateTime
import com.opencsv.CSVReaderHeaderAware
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.PoseSequence
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.KeyPoint
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.Person
import sensors_in_paradise.sonar.screen_recording.LoggingManager
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.BodyPart
import java.io.BufferedReader
import java.io.IOException
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter

class PoseEstimationStorageManager(var csvFile: File) {
    private val separator = ", "
    private var fileWriter: FileWriter? = FileWriter(csvFile)

    fun reset(newCsvFile: File): PoseEstimationStorageManager {
        csvFile = newCsvFile
        fileWriter = FileWriter(csvFile)

        return this
    }

    fun closeFile() {
        fileWriter?.close()
        fileWriter = null
    }

    fun writeHeader(startTime: LocalDateTime, captureResolution: Pair<Int, Int>, modelType: String, dimensions: Int) {
        val header = listOf<String>(
            "sep=$separator",
            "CaptureResolution: ${captureResolution.first}x${captureResolution.second}",
            "ModelType: $modelType",
            "Dimensions: $dimensions",
            "StartTime: $startTime",
            "StartTimeStamp: ${LoggingManager.normalizeTimeStamp(startTime)}",
        ).joinToString("\n", "", "\n")

        fileWriter?.appendLine("HeaderSize = ${header.length}")
        fileWriter?.appendLine(header)

        val columns = BodyPart.values().joinToString(
            ",",
            "TimeStamp,Confidence,",
            transform = { bp -> "${bp}_X,${bp}_Y" })
        fileWriter?.appendLine(columns)
    }

    fun storePoses(persons: List<Person>) {
        val person = persons.getOrNull(0)
        if (person != null) {
            val timeStamp = LoggingManager.normalizeTimeStamp(LocalDateTime.now())
            val confidence = person.score
            val keyPoints = person.keyPoints
            val outputLine = keyPoints.joinToString(
                ",",
                "$timeStamp,$confidence,",
                transform = { kp -> "${kp.coordinate.x},${kp.coordinate.y}" })
            fileWriter?.appendLine(outputLine)
        }
    }

    companion object {
        private fun extractStartTimeFromCSV(context: Context, inputFile: String): Long {
            val fileReader = BufferedReader(FileReader(inputFile))
            try {
                var line = fileReader.readLine()
                while (!line.contains("StartTimeStamp")) {
                    line = fileReader.readLine()
                }
                fileReader.close()
                return line.filter { it.isDigit() }.toLong()
            } catch (_: NumberFormatException) {
                Toast.makeText(
                    context,
                    "CSV Header corrupt",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return 0L
        }

        private fun getHeaderAwareFileReader(context: Context, inputFile: String): FileReader {
            val fileReader = FileReader(inputFile)
            try {
                var headerSize = ""
                var c = fileReader.read().toChar()
                while (c != '\n') {
                    c = fileReader.read().toChar()
                    if (c.isDigit()) {
                        headerSize += c
                    }
                }
                fileReader.skip(headerSize.toLong() + 1)
            } catch (_: NumberFormatException) {
                Toast.makeText(
                    context,
                    "CSV Header corrupt",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return fileReader
        }

        private fun personsFromCSVLine(line: Map<String, String>): List<Person> {
            var personConfidence = line["Confidence"]!!.toFloat()
            val keyPoints = BodyPart.values().map { bp ->
                try {
                    val x = line["${bp}_X"]!!.toFloat()
                    val y = line["${bp}_Y"]!!.toFloat()
                    KeyPoint(bp, PointF(x, y), 1f)
                } catch (_: Exception) {
                    personConfidence = .1f
                    KeyPoint(bp, PointF(.5f, .5f), .5f)
                }
            }
            return listOf<Person>(Person(0, keyPoints, null, personConfidence))
        }

        fun loadPoseSequenceFromCSV(context: Context, inputFile: String): PoseSequence {
            val csvData = PoseSequence(
                ArrayList<Long>(),
                ArrayList<List<Person>>(),
                0L
            )
            try {
                csvData.startTime = extractStartTimeFromCSV(context, inputFile)
                val fileReader = getHeaderAwareFileReader(context, inputFile)
                val csvReader = CSVReaderHeaderAware(fileReader)

                var line: Map<String, String>? = mapOf("_" to "")
                while (line != null) {
                    try {
                        line = csvReader.readMap()
                        csvData.timeStamps.add(line["TimeStamp"]!!.toLong())
                        csvData.personsArray.add(personsFromCSVLine(line))
                    } catch (_: Exception) {
                        break
                    }
                }
                csvReader.close()
            } catch (_: IOException) {
                Toast.makeText(
                    context,
                    "CSV File corrupt",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: FileNotFoundException) {
                Toast.makeText(
                    context,
                    "CSV File corrupt",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (_: NumberFormatException) {
                Toast.makeText(
                    context,
                    "CSV File corrupt",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return csvData
        }
    }
}
