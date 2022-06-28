package sensors_in_paradise.sonar.screen_recording

import com.opencsv.CSVReaderHeaderAware
import sensors_in_paradise.sonar.machine_learning.InMemoryWindow
import java.io.File
import java.io.FileReader
import java.io.IOException

class RecordingDataFile @Throws(IOException::class) constructor(private val mergedSensorDataFile: File) {
    private val indexesOfActivityChanges = findIndexesOfActivityChanges()

    class WindowException(msg: String) : Exception(msg)

    @Throws(WindowException::class)
    fun getWindowAtIndex(
        startingIndex: Int,
        windowSize: Int,
        featuresWithSensorTagPrefix: Array<String>
    ): Pair<InMemoryWindow, String> {
        val csvReader = getResetCsvReader()
        csvReader.skip(startingIndex)
        val window = InMemoryWindow(featuresWithSensorTagPrefix, windowSize)
        var activity: String? = null
        val errorMsgPrefix = "Window can't be filled from start line index $startingIndex: "
        for (i in 0 until (windowSize)) {
            val line: Map<String, String> = optNextLine(csvReader)
                ?: throw WindowException("$errorMsgPrefix Line is null before reaching window_size $windowSize")
            val lineActivity = line["activity"]
                ?: throw WindowException("$errorMsgPrefix Line without activity detected")
            val stf = line["SampleTimeFine"]?.replace(" ", "")?.toLong()
                ?: throw WindowException("$errorMsgPrefix Can't infer SampleTimeFine from line")

            if (activity == null) {
                activity = lineActivity
            }
            if (activity != lineActivity) {
                throw WindowException("$errorMsgPrefix Multiple activities " +
                        "within window have been detected: $lineActivity, $activity")
            }
            for ((feature, valueStr) in line) {
                if (window.needsFeature(feature)) {
                    val value = valueStr.replace(" ", "").toFloat()
                    window.appendSensorData(feature, value, stf)
                }
            }
        }
        return Pair(window, activity!!)
    }

    private fun getResetCsvReader(): CSVReaderHeaderAware {
        return CSVReaderHeaderAware(FileReader(mergedSensorDataFile))
    }

    fun getWindowStartIndexes(
        windowSize: Int
    ): ArrayList<Int> {
        val indexes = ArrayList<Int>()

        for (i in 0 until (indexesOfActivityChanges.size - 1)) {
            //TODO: also include interval from last change index to end of recording
            val startIndex = indexesOfActivityChanges[i]
            val endIndex = indexesOfActivityChanges[i + 1] - 1

            val activityLength = endIndex - startIndex
            if (activityLength < windowSize) {
                continue
            }
            val numCompletelyFittingWindows = activityLength / windowSize
            for (j in 0 until (numCompletelyFittingWindows)) {
                val windowStartIndex = startIndex + j * windowSize
                indexes.add(windowStartIndex)
                // 50% overlap
                val overlappingWindowStartIndex = windowStartIndex + windowSize / 2
                if (overlappingWindowStartIndex + windowSize <= endIndex) {
                    indexes.add(overlappingWindowStartIndex)
                }
            }
        }
        return indexes
    }

    private fun findIndexesOfActivityChanges(): ArrayList<Int> {
        val indexesOfActivityChanges = ArrayList<Int>()
        val csvReader = getResetCsvReader()
        var lastActivity: String? = null
        var line: Map<String, String>? = optNextLine(csvReader)

        var i = 0
        while (line != null) {
            val activity = line["activity"]
            if (activity != lastActivity) {
                indexesOfActivityChanges.add(i)
                lastActivity = activity
            }
            i++
            line = optNextLine(csvReader)
        }
        indexesOfActivityChanges.add(i-1)
        return indexesOfActivityChanges
    }

    private fun optNextLine(csvReader: CSVReaderHeaderAware): Map<String, String>? {
        return csvReader.readMap()
    }
}
