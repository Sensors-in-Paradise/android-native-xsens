package sensors_in_paradise.sonar.machine_learning

import sensors_in_paradise.sonar.ObservableArrayList
import sensors_in_paradise.sonar.screen_recording.RecordingDataFile

open class DataSet : ObservableArrayList<RecordingDataFile>() {

    fun convertToBatches(
        windowsPerBatch: Int,
        windowSize: Int,
        shuffle: Boolean = true,
        progressCallback: ((Int) -> Unit)? = null
    ): ArrayList<Batch> {
        val windowStartIndexesPerRecording = this.map { it.getWindowStartIndexes(windowSize) }

        val indexesOfIndexes = ArrayList<Pair<Int, Int>>()
        for ((recordingIndex, startIndexes) in windowStartIndexesPerRecording.withIndex()) {
            for (startIndex in startIndexes) {
                indexesOfIndexes.add(Pair(recordingIndex, startIndex))
            }
        }
        if (shuffle) {
            indexesOfIndexes.shuffle()
        }
        val numWindows = indexesOfIndexes.size
        val numBatches = numWindows / windowsPerBatch
        val result = ArrayList<Batch>(numBatches)
        for (b in 0 until numBatches) {
            val batchStartIndexesPerRecording = ArrayList(this.map { ArrayList<Int>() })
            for (i in b * windowsPerBatch until b * windowsPerBatch + windowsPerBatch) {
                val (recordingIndex, windowStartIndex) = indexesOfIndexes[i]
                batchStartIndexesPerRecording[recordingIndex].add(windowStartIndex)
            }
            result.add(Batch(this, batchStartIndexesPerRecording))
            progressCallback?.let { it((b * 100) / numBatches) }
        }
        return result
    }
}
