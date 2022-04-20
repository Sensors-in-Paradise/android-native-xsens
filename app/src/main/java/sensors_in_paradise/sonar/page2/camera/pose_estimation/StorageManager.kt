package sensors_in_paradise.sonar.page2.camera.pose_estimation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
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
import sensors_in_paradise.sonar.page2.camera.pose_estimation.data.KeyPoint
import java.io.FileReader

class StorageManager(val outputFile: File) {
    companion object {
        const val POSE_CAPTURE_FILENAME = "poseEstimation.csv"
    }

    private val separator = ", "
    private var headerSize: Int? = null
    private val fileWriter = FileWriter(outputFile)

    fun writeHeader(startTime: LocalDateTime, modelType: String, dimensions: Int) {
        val header = listOf<String>(
            "sep=$separator",
            "ModelType: $modelType",
            "Dimensions: $dimensions",
            "StartTime: $startTime",
        ).joinToString("\n", "", "\n")

        headerSize = header.length
        fileWriter.write(header)

        val columns = BodyPart.values().joinToString(
            ",",
            "TimeStamp,",
            transform = { bp -> "${bp}_X,${bp}_Y" })
        fileWriter.appendLine(columns)
    }

    fun storePoses(persons: List<Person>) {
        val keyPoints = persons.getOrNull(0)?.keyPoints
        if (keyPoints != null) {
            val timeStamp = LoggingManager.normalizeTimeStamp(LocalDateTime.now())
            val outputLine = keyPoints.joinToString(
                ",",
                "$timeStamp,",
                transform = { kp -> "${kp.coordinate.x},${kp.coordinate.y}" })
            fileWriter.appendLine(outputLine)
        }
    }

    private fun personsFromCSVLine(line: Map<String, String>): List<Person> {
        var personScore = 1f
        val keyPoints = BodyPart.values().map { bp ->
            try {
                val x = line["${bp}_X"]!!.toFloat()
                val y = line["${bp}_Y"]!!.toFloat()
                KeyPoint(bp, PointF(x, y), 1f)
            } catch (_: Exception) {
                personScore = .5f
                KeyPoint(bp, PointF(.5f, .5f), .5f)
            }
        }
        return listOf<Person>(Person(0, keyPoints, null, personScore))
    }

    // TODO sometimes it crashes because the csv file is empty (maybe move operation isnt done yet)
    fun createVideoFromCSV(inputFile: String, outputFile: File) {
        val width = 480
        val height = 640
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        var out: FileChannelWrapper? = null
        try {
            val fileReader = FileReader(inputFile)
            fileReader.skip(headerSize!!.toLong())
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
                        VisualizationUtils.Transformation.PROJECT_ON_CANVAS
                    )
                    VisualizationUtils.drawBodyKeypoints(canvas, persons, 2f, 1f)
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
