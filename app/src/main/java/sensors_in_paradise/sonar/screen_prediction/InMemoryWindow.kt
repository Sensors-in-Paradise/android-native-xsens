package sensors_in_paradise.sonar.screen_prediction

import android.util.Log
import com.xsens.dot.android.sdk.events.XsensDotData
import java.nio.ByteBuffer
import java.util.Collections.max

class InMemoryWindow(featuresWithSensorTagPrefix: Array<String>, val windowSize: Int = 90) :
    LinkedHashMap<String, ArrayList<Pair<Long, Float>>>() {

    private val featuresPerSensorTagPrefix =  LinkedHashMap<String, List<String>>()

    init {
        for (feature in featuresWithSensorTagPrefix) {
            put(feature.lowercase(), ArrayList(windowSize + cushion))
        }
        for (deviceTagPrefix in extractDeviceTagPrefixes(this.keys)) {
            featuresPerSensorTagPrefix[deviceTagPrefix] = extractFeatures(deviceTagPrefix, this.keys)
        }
    }

    fun appendSensorData(deviceTagPrefix: String, xsensDotData: XsensDotData) {
        val timeStamp: Long = xsensDotData.sampleTimeFine
        if(deviceTagPrefix !in featuresPerSensorTagPrefix){
            Log.w("InMemoryWindow-appendSensorData", "The device tag prefix $deviceTagPrefix for the given sensor data is not mentioned in the features that should be collected for this window.")
            return
        }
        for (featureTag in featuresPerSensorTagPrefix[deviceTagPrefix]!!) {
            val featureKey = "${featureTag}_${deviceTagPrefix}"
            val featureValues = this[featureKey]
            if (featureValues != null) {
                var insertionIndex = featureValues.size
                if (featureValues.isNotEmpty()) {
                    if (featureValues.last().first > timeStamp) {
                        insertionIndex = featureValues.indexOfLast { it.first < timeStamp } + 1
                    }
                }
                featureValues.add(
                    insertionIndex, Pair(
                        timeStamp,
                        getValueFromXsensDotData(featureTag, xsensDotData)
                    )
                )
            }
        }
    }

    private fun getValueFromXsensDotData(featureTag: String, xsensDotData: XsensDotData): Float {
        // Features can have tags with axis chars like Mag_Y, Quat_Z or dq_X
        // OR they can have index spelling like dv[1], dv[2] to determine their axis...
        val hasAxisIndexSpelling = featureTag.contains(indexSpellingRegex)
        val featureType =
            if (hasAxisIndexSpelling) featureTag.substringBefore("[") else featureTag.split("_")[0]
        val featureAxisTag = if (hasAxisIndexSpelling) featureTag.substringAfter("[")
            .substringBefore("]") else featureTag.split("_")[1]
        val featureAxisIndex = featureAxisTagToIndex(featureType, featureAxisTag)
        return when (featureType) {
            "quat" -> xsensDotData.quat[featureAxisIndex]
            "acc" -> xsensDotData.acc[featureAxisIndex].toFloat()
            "mag" -> xsensDotData.mag[featureAxisIndex].toFloat()
            "gyr" -> xsensDotData.gyr[featureAxisIndex].toFloat()
            "dq" -> xsensDotData.dq[featureAxisIndex].toFloat()
            "dv" -> xsensDotData.dv[featureAxisIndex].toFloat()
            else -> throw IllegalArgumentException("Unknown feature type: $featureType")
        }
    }

    private fun featureAxisTagToIndex(featureType: String, featureAxisTag: String): Int {
        return when (featureAxisTag) {
            "w" -> 0
            "x" -> 1
            "y" -> 2
            "z" -> 3
            "1" -> 0
            "2" -> 1
            "3" -> 2
            else -> throw IllegalArgumentException("Invalid feature axis")
        } - if (featureType == "quat" || featureType == "dq") 0 else 1
    }

    private fun extractDeviceTagPrefixes(keys: MutableSet<String>): List<String> {
        return keys.map { it.substringAfterLast("_") }.distinct()
            .toList()
    }

    private fun extractFeatures(deviceTagPrefix: String, keys: MutableSet<String>): List<String> {
        return keys.filter { it.endsWith(deviceTagPrefix) }.map { it.substringBeforeLast("_") }
            .toList()
    }

    fun compileWindow(): ByteBuffer {
        //find starting indices of feature vectors closest to the starting timestamp
        val startingIndices: Array<Int> = getStartIndices()

        //return byteBuffer of feature vectors starting at the starting indices
        val buffer = ByteBuffer.allocate(this.values.size * windowSize * bytesPerFloat)
        for (i in 0 until windowSize) {
            for ((j, key) in this.keys.withIndex()) {
                val index = startingIndices[j] + i
                if (index < this[key]!!.size) {
                    buffer.putFloat(this[key]!![index].second)
                } else {
                    throw IllegalStateException("Not enough data to fill window for feature: $key")
                }
            }
        }
        buffer.rewind()
        return buffer
    }

    fun clearValues() {
        for (key in this.keys) {
            this[key]?.clear()
        }
    }

    private fun getStartIndices(): Array<Int> {
        //find latest starting timestamp of each feature
        val startingTimestamp: Long = max(this.values.map { it[0].first })
        //find starting indices of feature vectors closest to the starting timestamp
        return this.values.map {
            findClosestIndex(startingTimestamp, it)
        }.toTypedArray()
    }

    private fun hasEnoughDataToCompileWindow(startIndices: Array<Int>): Boolean {
        for ((i, key) in this.keys.withIndex()) {
            if (startIndices[i] + windowSize > (this[key]?.size ?: 0)) {
                return false
            }
        }
        return true
    }

    fun hasEnoughDataToCompileWindow(): Boolean {
        return hasEnoughDataToCompileWindow(getStartIndices())
    }

    private fun findClosestIndex(
        startingTimestamp: Long,
        valuesSorted: ArrayList<Pair<Long, Float>>
    ): Int {
        var closestIndex = 0
        var closestDifference = Long.MAX_VALUE
        for (i in 0 until valuesSorted.size) {
            val difference = Math.abs(valuesSorted[i].first - startingTimestamp)
            if (difference <= closestDifference) {
                closestIndex = i
                closestDifference = difference
            } else {
                return closestIndex
            }
        }
        return closestIndex
    }

    companion object {
        private const val cushion = 10
        private const val bytesPerFloat = 4
        private val indexSpellingRegex = Regex("\\[\\d\\]")
    }
}