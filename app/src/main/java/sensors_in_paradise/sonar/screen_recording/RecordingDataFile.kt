package sensors_in_paradise.sonar.screen_recording

import com.opencsv.CSVReaderHeaderAware
import sensors_in_paradise.sonar.screen_prediction.InMemoryWindow
import java.io.File
import java.io.FileReader
import java.io.IOException

class RecordingDataFile @Throws(IOException::class) constructor(private val mergedSensorDataFile: File) {
    private val indexesOfActivityChanges = findIndexesOfActivityChanges()

    class WindowException(msg: String) : Exception(msg)

    @Throws(WindowException::class)
    fun getWindowAtIndex(
        startingIndex: Int,
        window_size: Int,
        featuresWithSensorTagPrefix: Array<String>
    ): Pair<InMemoryWindow, String> {
        val csvReader = getResetCsvReader()
        csvReader.skip(startingIndex)
        val window = InMemoryWindow(featuresWithSensorTagPrefix, window_size)
        var activity: String? = null
        for (i in 0 until (window_size)) {
            val line: Map<String, String> = optNextLine(csvReader)
                ?: throw WindowException("Window can't be filled from start line index $startingIndex: Line is null before reaching window_size $window_size")
            val lineActivity = line["activity"]
                ?: throw WindowException("Window can't be filled from start line index $startingIndex: Line without activity detected")
            val stf = line["SampleTimeFine"]?.toLong()
                ?: throw WindowException("Window can't be filled from start line index $startingIndex: Can't infer SampleTimeFine from line")

            if (activity == null) {
                activity = lineActivity
            }
            if (activity != lineActivity) {
                throw WindowException("Window can't be filled from start line index $startingIndex: Multiple activities within window have been detected: $lineActivity, $activity")
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
        return CSVReaderHeaderAware( FileReader(mergedSensorDataFile))
    }

    fun getWindowStartIndexes(
        window_size: Int,
        minOverlapForCutOffWindow: Float = 0.5f
    ): ArrayList<Int> {
        val indexes = ArrayList<Int>()

        for (i in 0 until (indexesOfActivityChanges.size - 1)) {
            val startIndex = indexesOfActivityChanges[i]
            val endIndex = indexesOfActivityChanges[i + 1] - 1

            val activityLength = endIndex - startIndex
            if (activityLength < window_size) {
                continue
            }
            val numCompletelyFittingWindows = activityLength / window_size
            for (j in 0 until (numCompletelyFittingWindows)) {
                indexes.add(startIndex + j * window_size)
            }
            if (activityLength - numCompletelyFittingWindows >= window_size * minOverlapForCutOffWindow) {
                indexes.add(endIndex - window_size)
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
        return indexesOfActivityChanges
    }

    private fun optNextLine(csvReader: CSVReaderHeaderAware): Map<String, String>? {
        return csvReader.readMap()
    }
}