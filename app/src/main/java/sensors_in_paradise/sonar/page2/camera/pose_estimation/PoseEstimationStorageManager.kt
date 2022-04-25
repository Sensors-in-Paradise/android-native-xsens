package sensors_in_paradise.sonar.page2.camera.pose_estimation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.util.Log
import org.jcodec.api.android.AndroidSequenceEncoder
import sensors_in_paradise.sonar.page2.LoggingManager
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.BodyPart
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.Person
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import org.jcodec.common.io.FileChannelWrapper
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import com.opencsv.CSVReaderHeaderAware
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.PoseSequence
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.KeyPoint
import java.io.BufferedReader
import java.io.FileReader

class PoseEstimationStorageManager(var csvFile: File) {
    private val separator = ", "

    private var fileWriter: FileWriter? = FileWriter(csvFile)

    fun reset(newCsvFile: File): PoseEstimationStorageManager {
        csvFile = newCsvFile
        fileWriter = FileWriter(csvFile)

        return this
    }

    fun writeHeader(startTime: LocalDateTime, modelType: String, dimensions: Int) {
        val header = listOf<String>(
            "sep=$separator",
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

    fun closeFile() {
        fileWriter?.close()
        fileWriter = null
    }

    companion object {
        private fun extractStartTimeFromCSV(inputFile: String): Long {
            try {
                val fileReader = BufferedReader(FileReader(inputFile))
                var line = fileReader.readLine()
                while (!line.contains("StartTimeStamp")) {
                    line = fileReader.readLine()
                }
                fileReader.close()
                return line.filter { it.isDigit() }.toLong()
            } catch (_: Exception) {
                throw Exception("CSV header corrupt")
            }
        }

        private fun getHeaderAwareFileReader(inputFile: String): FileReader {
            try {
                val fileReader = FileReader(inputFile)
                var headerSize = ""
                var c = fileReader.read().toChar()
                while (c != '\n') {
                    c = fileReader.read().toChar()
                    if (c.isDigit()) {
                        headerSize += c
                    }
                }
                fileReader.skip(headerSize.toLong() + 1)
                return fileReader
            } catch (_: Exception) {
                throw Exception("CSV header corrupt")
            }
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

        fun loadPoseSequenceFromCSV(inputFile: String): PoseSequence {
            val csvData = PoseSequence(
                ArrayList<Long>(),
                ArrayList<List<Person>>(),
                extractStartTimeFromCSV(inputFile)
            )
            try {
                val fileReader = getHeaderAwareFileReader(inputFile)
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
            } catch (e: Exception) {
                // TODO use Toasts or whatever to handle exception right (and prevent app crash)
                Log.d("PoseEstimation", e.toString())
            }
            return csvData
        }

        // TODO delete if not needed in the end
        fun createVideoFromCSV(inputFile: String, outputFile: File) {
            val width = 480
            val height = 640
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            var out: FileChannelWrapper? = null
            try {
                val fileReader = getHeaderAwareFileReader(inputFile)
                val csvReader = CSVReaderHeaderAware(fileReader)

                out = NIOUtils.writableFileChannel(outputFile.absolutePath)
                val encoder = AndroidSequenceEncoder(out, Rational.R(15, 1))

                var line = csvReader.readMap()
                while (line != null) {
                    try {
                        line = csvReader.readMap()
                        val persons = personsFromCSVLine(line)
                        VisualizationUtils.transformKeypoints(
                            persons, bitmap, canvas,
                            VisualizationUtils.Transformation.PROJECT_ON_CANVAS,
                            false
                        )
                        VisualizationUtils.drawBodyKeypoints(
                            persons,
                            canvas,
                            circleRadius = 2f,
                            lineWidth = 1f
                        )
                        encoder.encodeImage(bitmap)
                    } catch (_: Exception) {
                        break
                    }
                }
                encoder.finish()
            } finally {
                NIOUtils.closeQuietly(out)
            }
        }
    }
}
