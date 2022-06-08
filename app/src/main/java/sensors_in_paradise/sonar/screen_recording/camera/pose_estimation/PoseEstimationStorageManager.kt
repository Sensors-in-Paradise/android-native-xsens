package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import android.content.Context
import android.graphics.PointF
import android.widget.Toast
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList
import java.time.LocalDateTime
import com.opencsv.CSVReaderHeaderAware
import sensors_in_paradise.sonar.screen_recording.LoggingManager
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.*
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

    fun writeHeader(
        startTime: LocalDateTime,
        captureResolution: Pair<Int, Int>,
        modelType: String,
        dimensions: Int,
        poseType: Pose
    ) {
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

        val columns = getCSVColumns(poseType).joinToString(",")
        fileWriter?.appendLine(columns)
    }

    fun storeBodyPoses(persons: List<Person>) {
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

    fun storeHandPoses(hands: List<NormalizedLandmarkList>, handsLabels: List<String>) {
        if (hands.isEmpty())
            return

        val timeStamp = LoggingManager.normalizeTimeStamp(LocalDateTime.now())
        var outputLine = timeStamp.toString()

        // Storing maximum of one left and one right hand
        listOf("Left", "Right").forEach { label ->
            val index = hands.indices.find { handsLabels[it] == label }
            val landmarkList = index?.let { hands[index] }

            HandPart.values().forEach { handPart ->
                val landmark = landmarkList?.getLandmark(handPart.position)
                outputLine +=
                    if (landmark != null)
                        ",${landmark.x},${landmark.y},${landmark.z}"
                    else
                        ",,,"
            }
        }
        fileWriter?.appendLine(outputLine)
    }

    companion object {
        private fun getCSVColumns(poseType: Pose): List<String> {
            val columns = mutableListOf("TimeStamp")
            when (poseType) {
                Pose.BodyPose -> {
                    columns.add("Confidence")
                    BodyPart.values().forEach { bp ->
                        columns.addAll(listOf("${bp}_X", "${bp}_Y"))
                    }
                }
                Pose.HandPose -> {
                    listOf("LEFT", "RIGHT").forEach { side ->
                        HandPart.values().forEach { hp ->
                            columns.add("${side}_${hp}_X")
                            columns.add("${side}_${hp}_Y")
                            columns.add("${side}_${hp}_Z")
                        }
                    }
                }
            }
            return columns
        }

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

        private fun extractModelTypeFromCSV(context: Context, inputFile: String): String {
            val fileReader = BufferedReader(FileReader(inputFile))
            try {
                var line = fileReader.readLine()
                while (!line.contains("ModelType")) {
                    line = fileReader.readLine()
                }
                fileReader.close()
                return line.split(": ", "\n")[1]
            } catch (_: IndexOutOfBoundsException) {
                Toast.makeText(
                    context,
                    "CSV Header corrupt",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return ""
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
            return listOf(Person(0, keyPoints, null, personConfidence))
        }

        private fun hand2DPointsFromCSVLine(line: Map<String, String>): List<List<PointF>?> {
            val hands = mutableListOf<List<PointF>?>()
            listOf("LEFT", "RIGHT").forEach { side ->
                try {
                    val handPoints = HandPart.values().map { handPart ->
                        val x = line["${side}_${handPart}_X"]!!.toFloat()
                        val y = line["${side}_${handPart}_Y"]!!.toFloat()
                        PointF(x, y)
                    }
                    hands.add(handPoints)
                } catch (_: Exception) {
                    hands.add(null)
                }
            }
            return hands
        }

        private fun posesFromCSVLine(
            line: Map<String, String>,
            poseType: Pose
        ): List<List<PointF>?> {
            return when (poseType) {
                Pose.BodyPose -> VisualizationUtils.convertTo2DPoints(personsFromCSVLine(line))
                Pose.HandPose -> hand2DPointsFromCSVLine(line)
            }
        }

        fun loadPoseSequenceFromCSV(context: Context, inputFile: String): PoseSequence {
            var startTime = 0L
            var poseType = Pose.BodyPose
            val timeStamps = ArrayList<Long>()
            val posesArray = ArrayList<List<List<PointF>?>>()

            try {
                startTime = extractStartTimeFromCSV(context, inputFile)
                poseType = if (
                    extractModelTypeFromCSV(context, inputFile) == HandDetector.MODEL_NAME
                ) Pose.HandPose
                else Pose.BodyPose

                val fileReader = getHeaderAwareFileReader(context, inputFile)
                val csvReader = CSVReaderHeaderAware(fileReader)

                var line: Map<String, String>? = mapOf("_" to "")
                while (line != null) {
                    try {
                        line = csvReader.readMap()
                        timeStamps.add(line["TimeStamp"]!!.toLong())
                        posesArray.add(posesFromCSVLine(line, poseType))
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
            return PoseSequence(
                timeStamps,
                posesArray,
                startTime,
                poseType
            )
        }
    }
}
