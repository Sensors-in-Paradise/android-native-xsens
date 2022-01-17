package sensors_in_paradise.sonar.util

import android.content.Context
import android.widget.Toast
import sensors_in_paradise.sonar.GlobalValues
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

class PredictionHelper(
    private val context: Context,
    private val rawSensorDataMap: MutableMap<String, MutableList<Pair<Long, FloatArray>>>
) {
    private val sizeOfFloat = 4
    private val numQuats = 4
    private val numFreeAccs = 3
    private var numDataLines = 0

    private val dataLineByteSize = sizeOfFloat * (numQuats + numFreeAccs) * GlobalValues.NUM_DEVICES
    val dataLineFloatSize = (numQuats + numFreeAccs) * GlobalValues.NUM_DEVICES
    val dataVectorSize = 180

    private fun fillEmptyDataLines() {
        val frequency = 60
        val epsilon = 10

        // '!!' i.O., because sensor data gets checked for null lists before
        val startingTimestamp = rawSensorDataMap.maxOf { it.value.first().first }
        val finishingTimestamp = rawSensorDataMap.minOf { it.value.last().first }

        if (finishingTimestamp <= startingTimestamp) {
            Toast.makeText(context, "Timestamps not in sync", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "Data may be inconsistent!", Toast.LENGTH_SHORT).show()
            return
        }

        // cut off every entry outside of 'starting-' and 'finishingTimestamp'
        // use 'startingEntries' to ensure having a starting value if entry at 'startingTimestamp' is missing
        val startingEntries = mutableMapOf<String, FloatArray>()
        for ((deviceAddress, deviceDataList) in rawSensorDataMap) {

            while (deviceDataList.first().first < startingTimestamp - epsilon) {
                startingEntries[deviceAddress] = deviceDataList.first().second
                deviceDataList.removeFirst()
            }

            while (deviceDataList.last().first > finishingTimestamp + epsilon) {
                deviceDataList.removeLast()
            }
        }

        // Fill empty data lines for all devices
        val numLines = (((finishingTimestamp - startingTimestamp) * frequency) / 1000000).toInt()
        val timeStep = 1000000.toDouble() / frequency.toDouble()
        for ((deviceAddress, oldDeviceDataList) in rawSensorDataMap) {
            val newDeviceDataList = mutableListOf<Pair<Long, FloatArray>>()

            // ensure entry at 'startingTimestamp'
            if (oldDeviceDataList.first().first > startingTimestamp + epsilon) {
                val fillEntry = Pair(startingTimestamp, startingEntries[deviceAddress]!!)
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
            rawSensorDataMap[deviceAddress] = newDeviceDataList
        }
    }

    private fun normalizeLine(dataArray: FloatArray, minArray: DoubleArray, maxArray: DoubleArray): FloatArray {
        val numElements = numQuats + numFreeAccs
        val normalizedArray = FloatArray(numElements)

        val lowerBound = 0.0001
        val upperBound = 0.9999
        for (i in 0 until numElements) {
            val rawNormalize = (dataArray[i].toDouble() - minArray[i]) / (maxArray[i] - minArray[i])
            val clippedNormalize = max(min(upperBound, rawNormalize), lowerBound)

            normalizedArray[i] = clippedNormalize.toFloat()
        }
        return normalizedArray
    }

    @Suppress("MaxLineLength", "TooGenericExceptionCaught", "SwallowedException", "ReturnCount")
    fun processSensorData(): ByteBuffer? {
        // check for sensors without data
        rawSensorDataMap.forEach {
            val tag = GlobalValues.sensorAddressToTag(it.key)
            val listLen = it.value.size
            if (listLen == 0) {
                UIHelper.showAlert(context, "\'$tag\' did not collect data!")
                return null
            }
        }

        // fill empty data lines
        try {
            fillEmptyDataLines()
        } catch (e: Exception) {
            Toast.makeText(context, "Filling of empty data failed", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "Data may be inconsistent!", Toast.LENGTH_SHORT).show()
        }

        // check for minimal length
        val minDataLines = rawSensorDataMap.minOfOrNull { it.value.size }!!
        if (minDataLines < dataVectorSize) {
            Toast.makeText(context, "Not enough data collected!", Toast.LENGTH_SHORT).show()
            return null
        }

        numDataLines = dataVectorSize

        // normalize
        var floatArray = FloatArray(0)
        for (row in 0 until numDataLines) {
            var lineFloatArray = FloatArray(0)
            for ((deviceAddress, deviceDataList) in rawSensorDataMap) {

                var normalizedFloatArray = FloatArray(0)
                when (deviceAddress) {
                    GlobalValues.sensorTagMap["LF"] -> normalizedFloatArray = normalizeLine((deviceDataList[row].second), doubleArrayOf(-0.8126836, -0.79424906, -0.7957623, -0.8094078, -31.278593, -32.166283, -18.486694), doubleArrayOf(0.8145418, 0.79727143, 0.81989765, 0.8027102, 28.956848, 30.199568, 22.69250))
                    GlobalValues.sensorTagMap["LW"] -> normalizedFloatArray = normalizeLine((deviceDataList[row].second), doubleArrayOf(-0.8398707, -0.8926556, -0.9343553, -0.9552342, -11.258037, -10.1190405, -8.37381), doubleArrayOf(0.7309214, 0.9186623, 0.97258735, 0.9084077, 10.640987, 11.26736, 12.94717))
                    GlobalValues.sensorTagMap["ST"] -> normalizedFloatArray = normalizeLine((deviceDataList[row].second), doubleArrayOf(-0.87042844, -0.6713179, -0.6706054, -0.80093706, -20.164385, -20.21316, -8.670398), doubleArrayOf(0.87503606, 0.686213, 0.67588365, 0.8398282, 15.221635, 13.93141, 11.75221))
                    GlobalValues.sensorTagMap["RW"] -> normalizedFloatArray = normalizeLine((deviceDataList[row].second), doubleArrayOf(-0.9208972, -0.8918428, -0.9212201, -0.9103423, -14.090326, -14.17955, -11.573973), doubleArrayOf(0.93993384, 0.888225, 0.9099328, 0.9181471, 14.901558, 11.34146, 15.649994))
                    GlobalValues.sensorTagMap["RF"] -> normalizedFloatArray = normalizeLine((deviceDataList[row].second), doubleArrayOf(-0.8756618, -0.85241073, -0.8467437, -0.8629473, -31.345306, -31.825573, -16.296654), doubleArrayOf(0.8837259, 0.98513246, 0.9278882, 0.8547427, 31.27872, 30.43604, 20.430))
                    else -> { // Note the block
                        UIHelper.showAlert(context, "Unknown Device!")
                    }
                }

                lineFloatArray += normalizedFloatArray
            }
            floatArray += lineFloatArray
        }

        // create buffer
        val sensorDataByteBuffer = ByteBuffer.allocate(numDataLines * dataLineByteSize)
        sensorDataByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        sensorDataByteBuffer.asFloatBuffer().put(floatArray, 0, numDataLines * dataLineFloatSize)
        sensorDataByteBuffer.rewind()
        return sensorDataByteBuffer
    }
}
