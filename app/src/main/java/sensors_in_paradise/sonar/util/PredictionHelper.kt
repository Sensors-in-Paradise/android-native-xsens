package sensors_in_paradise.sonar.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import sensors_in_paradise.sonar.XSensDotDeviceWithOfflineMetadata
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.properties.Delegates

class PredictionHelper(
    private val context: Context,
    private val showToasts: Boolean,

    ) {
    lateinit var normalizationParams: HashMap<String, HashMap<String, ArrayList<Double>>>
    var numDevices by Delegates.notNull<Int>()
    private val sizeOfFloat = 4
    var numQuats by Delegates.notNull<Int>()
    var numAccs by Delegates.notNull<Int>()
    var numGyros by Delegates.notNull<Int>()
    private var numDataLines: Int = 0

    private var dataLineByteSize by Delegates.notNull<Int>()
    var dataLineFloatSize by Delegates.notNull<Int>()
    var dataVectorSize by Delegates.notNull<Int>()

    private fun calcDataLineFloatSize(): Int {
        return ((numQuats + numAccs + numGyros) * numDevices).apply { dataLineFloatSize = this }
    }

    fun calcDataLineByteSize(): Int {
        return (sizeOfFloat * calcDataLineFloatSize()).apply { dataLineByteSize = this }
    }

    private fun fillEmptyDataLines(rawSensorDataMap: MutableMap<String, MutableList<Pair<Long, FloatArray>>>) {
        val frequency = 60
        val epsilon = 10

        // '!!' i.O., because sensor data gets checked for null lists before
        val startingTimestamp = rawSensorDataMap.maxOf { it.value.first().first }
        val finishingTimestamp = rawSensorDataMap.minOf { it.value.last().first }

        if (finishingTimestamp <= startingTimestamp) {
            if (showToasts) {
                Toast.makeText(context, "Timestamps not in sync", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "Data may be inconsistent!", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // cut off every entry outside of 'starting-' and 'finishingTimestamp'
        // use 'startingEntries' to ensure having a starting value if entry at 'startingTimestamp' is missing
        val startingEntries = mutableMapOf<String, FloatArray>()
        for ((deviceTag, deviceDataList) in rawSensorDataMap) {

            while (deviceDataList.first().first < startingTimestamp - epsilon) {
                startingEntries[deviceTag] = deviceDataList.first().second
                deviceDataList.removeFirst()
            }

            while (deviceDataList.last().first > finishingTimestamp + epsilon) {
                deviceDataList.removeLast()
            }
        }

        // Fill empty data lines for all devices
        val numLines = (((finishingTimestamp - startingTimestamp) * frequency) / 1000000).toInt()
        val timeStep = 1000000.toDouble() / frequency.toDouble()
        for ((deviceTag, oldDeviceDataList) in rawSensorDataMap) {
            val newDeviceDataList = mutableListOf<Pair<Long, FloatArray>>()

            // ensure entry at 'startingTimestamp'
            if (oldDeviceDataList.first().first > startingTimestamp + epsilon) {
                val fillEntry = Pair(startingTimestamp, startingEntries[deviceTag]!!)
                newDeviceDataList.add(fillEntry.copy())
            } else {
                newDeviceDataList.add(oldDeviceDataList.first().copy())
            }

            // Iterating over all Timestamps from 'starting-' to 'finishingTimestamp'#
            // Using last values if one is missing
            // beginning at 1, because 0 entry is already filled and maybe needed for entry 1
            for (it in 1..numLines) {
                var isEntryNullOrEmpty = true
                if (oldDeviceDataList.getOrNull(it) != null) {
                    val lastTimestamp = oldDeviceDataList[it - 1].first
                    val currentTimestamp = oldDeviceDataList[it].first
                    isEntryNullOrEmpty = currentTimestamp - lastTimestamp > timeStep + epsilon
                }
                if (isEntryNullOrEmpty) {
                    val fillTimestamp = (newDeviceDataList.last().first + timeStep).toLong()
                    val fillValues = newDeviceDataList.last().second
                    val fillEntry = Pair(fillTimestamp, fillValues)

                    newDeviceDataList.add(fillEntry.copy())
                } else {
                    newDeviceDataList.add(oldDeviceDataList[it].copy())
                }
            }
            rawSensorDataMap[deviceTag] = newDeviceDataList
        }
    }

    private fun normalizeLine(
        dataArray: FloatArray,
        normalizationParams: java.util.HashMap<String, java.util.ArrayList<Double>>?
    ): FloatArray {
        val numElements = numQuats + numAccs + numGyros
        val normalizedArray = FloatArray(numElements)
        for (i in 0 until numElements) {
            normalizedArray[i] =
                ((dataArray[i].toDouble() - (normalizationParams?.get("mean")?.get(i)
                    ?: 0.0)) / (normalizationParams?.get("std")?.get(i) ?: 1.0)).toFloat()
        }
        return normalizedArray
    }

    @Suppress("MaxLineLength", "TooGenericExceptionCaught", "SwallowedException", "ReturnCount")
    fun processSensorData(rawSensorDataMap: MutableMap<String, MutableList<Pair<Long, FloatArray>>>): ByteBuffer? {
        // check for sensors without data

        Log.d("PredictionHelper-processRawSensorData", rawSensorDataMap.map { "${it.key}: ${it.value.size}" }.joinToString())
        rawSensorDataMap.forEach {
            val tag = it.key
            val listLen = it.value.size
            if (listLen == 0) {
                UIHelper.showAlert(context, "\'$tag\' did not collect data!")
                return null
            }
        }

        // fill empty data lines
        try {
            fillEmptyDataLines(rawSensorDataMap)
        } catch (e: Exception) {

            Toast.makeText(context, "Filling of empty data failed", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "Data may be inconsistent!", Toast.LENGTH_SHORT).show()
        }

        // check for minimal length
        val recordedLinesCount = rawSensorDataMap.minOfOrNull { it.value.size }!!
        if (recordedLinesCount < dataVectorSize) {
            Toast.makeText(context, "Not enough data collected!", Toast.LENGTH_SHORT).show()
            return null
        }

        numDataLines = dataVectorSize
        val predictionStartIndex = recordedLinesCount - numDataLines

        // normalize
        var floatArray = FloatArray(0)
        for (row in predictionStartIndex until recordedLinesCount) {
            var lineFloatArray = FloatArray(0)
            for ((deviceTag, deviceDataList) in rawSensorDataMap) {

                var normalizedFloatArray = FloatArray(0)


                when (XSensDotDeviceWithOfflineMetadata.extractTagPrefixFromTag(deviceTag)) {
                    "LF" -> normalizedFloatArray = normalizeLine(
                        (deviceDataList[row].second),
                        normalizationParams["LF"]
                    )
                    "RF" -> normalizedFloatArray = normalizeLine(
                        (deviceDataList[row].second),
                        normalizationParams["RF"]
                    )
                    "LW" -> normalizedFloatArray = normalizeLine(
                        (deviceDataList[row].second),
                        normalizationParams["LW"]
                    )
                    "RW" -> normalizedFloatArray = normalizeLine(
                        (deviceDataList[row].second),
                        normalizationParams["RW"]
                    )
                    "STS" -> normalizedFloatArray = normalizeLine(
                        (deviceDataList[row].second),
                        normalizationParams["ST"]
                    )
                    else -> { // Note the block
                        UIHelper.showAlert(context, "Unknown Device!")
                    }
                }

                // Take only free acc
                lineFloatArray += normalizedFloatArray.takeLast(3)
            }
            floatArray += lineFloatArray
        }

        // create buffer
        val sensorDataByteBuffer = ByteBuffer.allocate(numDataLines * dataLineByteSize).apply {
            order(ByteOrder.LITTLE_ENDIAN)
        }
        sensorDataByteBuffer.asFloatBuffer().put(floatArray, 0, numDataLines * dataLineFloatSize)
        sensorDataByteBuffer.rewind()
        return sensorDataByteBuffer
    }
}
