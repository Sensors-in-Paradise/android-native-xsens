package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.io.ColType
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.size
import sensors_in_paradise.sonar.screen_recording.Recording
import sensors_in_paradise.sonar.screen_recording.RecordingMetadataStorage
import sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data.Pose
import java.util.*
import java.util.Collections.min
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("SetTextI18n")
class SensorPlacementEstimator(
    val context: Context,
    estimatePlacementButton: Button,
    lessPositionsButton: Button,
    morePositionsButton: Button,
    numPositionsTV: TextView,
    private val numRecordingsTV: TextView,
    val onSelectedRecordingsChanged: ((List<Recording>) -> Unit)
) {
    val recordings = mutableListOf<Recording>()
    private var numPositions = 1

    companion object {
        val POSITIONS = SensorPlacementDialog.POSITIONS_MAP.keys
        const val MIN_FRAME_LENGTH = 500
        const val MIN_ESTIMATED_DURATION = 60
    }

    init {
        estimatePlacementButton.setOnClickListener {
            if (areRecordingsEligible()) {
                val scores = estimateSensorPlacements()
                scores?.let {
                    SensorPlacementDialog(context).updateScores(scores)
                }
            }
        }
        lessPositionsButton.setOnClickListener {
            // TODO
        }
        morePositionsButton.setOnClickListener {
            // TODO
        }
        numPositionsTV.text = "$numPositions Positions"
        numRecordingsTV.text = "1"
    }

    /**
     * Returns [true] if recording is being added to selected recordings, else [false]
     */
    fun toggleRecordingSelection(recording: Recording): Boolean {
        return if (recording in recordings) {
            removeRecording(recording)
            false
        } else {
            tryAddRecording(recording)
        }
    }

    private fun removeRecording(recording: Recording) {
        recordings.remove(recording)
        onRecordingUpdate()
    }

    private fun tryAddRecording(recording: Recording): Boolean {
        if (!isRecordingEligible(recording)) {
            return false
        }
        recordings.add(recording)
        onRecordingUpdate()
        return true
    }

    private fun onRecordingUpdate() {
        onSelectedRecordingsChanged(recordings)
        numRecordingsTV.text = "${recordings.size}"
    }

    /**
     * Checks whether a recording contains valuable data at all
     */
    private fun isRecordingEligible(recording: Recording): Boolean {
        try {
            if (!recording.hasPoseSequenceRecording()) {
                throw Exception("No Body Pose Sequence available.")
            }

            val poseFilePath = recording.getPoseSequenceFile().absolutePath
            if (PoseEstimationStorageManager.getPoseTypeFromCSV(context, poseFilePath)
                != Pose.BodyPose
            ) {
                throw Exception("Pose Sequence has to be of type 'Body Pose'.")
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    /**
     * Checks efficiently whether the selected recordings are likely to suffice the required amount of data
     */
    private fun areRecordingsEligible(): Boolean {
        try {
            val estimatedActivityTimes = mutableMapOf<String, Long>()
            recordings.forEach { recording ->
                val activities = recording.metadataStorage.getActivities()
                activities.forEachIndexed { index, labelEntry ->
                    val activityEndTime = activities.getOrNull(index + 1)?.timeStarted
                        ?: recording.metadataStorage.getTimeEnded()
                    val duration = activityEndTime - labelEntry.timeStarted

                    val oldDuration = estimatedActivityTimes.getOrDefault(labelEntry.activity, 0L)
                    estimatedActivityTimes[labelEntry.activity] = oldDuration + (duration / 1000)
                }
            }

            val filteredActivityTimes =
                estimatedActivityTimes.filter { (_, duration) -> duration >= MIN_ESTIMATED_DURATION }

            if (filteredActivityTimes.size < 2) {
                throw Exception("At least 2 Activities with 60s Capture time required.")
            }
        } catch (e: Exception) {
            Toast.makeText( // TODO
                context,
                e.message,
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun estimateSensorPlacements(): Map<String, Float>? {
        try {
            var (df, activities) = createMergedDataFrame()

            df = mergeColumns(df)

            df = appendActivities(df, activities)

            val activityFrames = getFramesPerActivity(df)

            if (activityFrames.size < 2) {
                throw Exception("Not enough data provided.")
            }

            val scores = POSITIONS.associateWith { position ->
                calcSensorScore(position, activityFrames.values.toList())
            }
            return scores

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Estimation Failed:\n ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            return null
        }
    }

    private fun calcScore(df1: DataColumn<Double>, df2: DataColumn<Double>): Float {
        val v1 = df1.asSequence()
        val v2 = df2.asSequence()

        val dotProduct = v1.zip(v2, Double::times).sum()
        val norm1 = sqrt(v1.map { it.pow(2) }.sum())
        val norm2 = sqrt(v2.map { it.pow(2) }.sum())

        val cosine = dotProduct / (norm1 * norm2)
        return abs(log2(abs(cosine).toFloat()))
    }

    private fun calcSensorScore(position: String, activityFrames: List<DataFrame<*>>): Float {
        var sensorScore = 0f
        var actIterator = activityFrames.size - 2
        activityFrames.forEach { frame1 ->
            val x1 = frame1["${position}_X"].convertToDouble().dropNulls()
            val y1 = frame1["${position}_Y"].convertToDouble().dropNulls()

            for (i in actIterator downTo 0) {
                val frame2 = activityFrames[i]
                val x2 = frame2["${position}_X"].convertToDouble().dropNulls()
                val y2 = frame2["${position}_Y"].convertToDouble().dropNulls()

                sensorScore += calcScore(x1, x2)
                sensorScore += calcScore(y1, y2)
            }

            actIterator -= 1
        }
        return sensorScore
    }

    private fun getFramesPerActivity(df: DataFrame<*>): Map<String, DataFrame<Any?>> {
        val activityGroups = df.groupBy("Activity")
        val activityFrames = activityGroups.keys.values().mapIndexedNotNull { index, activity ->
            val frame = activityGroups.groups[index]
            if (frame.size().nrow >= MIN_FRAME_LENGTH) {
                activity.toString() to frame
            } else null
        }.toMap()

        // Cut Frames into equal size, by taking only the first minFrameSize rows
        val minFrameSize = min(activityFrames.map { (_, frame) -> frame.size().nrow })
        return activityFrames.mapValues { (_, frame) ->
            frame.take(minFrameSize)
        }
    }

    private fun appendActivities(
        df: DataFrame<*>,
        activities: ArrayList<RecordingMetadataStorage.LabelEntry>
    ): DataFrame<*> {
        var iterator = 0
        val activityColumn = df.mapToColumn("Activity") { row ->
            val ts = (row["TimeStamp"] as Long)
            if (iterator == activities.size - 1 || ts < activities[iterator + 1].timeStarted) {
                activities[iterator].activity
            } else {
                iterator += 1
                activities[iterator].activity
            }
        }
        return df.plus(activityColumn)
    }

    private fun mergeColumns(df: DataFrame<*>): DataFrame<*> {
        var mergedDf = df

        // Merge Head Points
        mergedDf =
            mergedDf.merge { ("NOSE_X" and "LEFT_EYE_X" and "RIGHT_EYE_X" and "LEFT_EAR_X" and "RIGHT_EAR_X") as ColumnSet<Double> }
                .by { it.average() }.into("HEAD_X")
        mergedDf =
            mergedDf.merge { ("NOSE_Y" and "LEFT_EYE_Y" and "RIGHT_EYE_Y" and "LEFT_EAR_Y" and "RIGHT_EAR_Y") as ColumnSet<Double> }
                .by { it.average() }.into("HEAD_Y")

        //Merge Hip Points
        mergedDf = mergedDf.merge { ("LEFT_HIP_X" and "RIGHT_HIP_X") as ColumnSet<Double> }
            .by { it.average() }.into("HIP_X")
        mergedDf = mergedDf.merge { ("LEFT_HIP_Y" and "RIGHT_HIP_Y") as ColumnSet<Double> }
            .by { it.average() }.into("HIP_Y")

        return mergedDf
    }

    private fun appendPoseFileToDataFrame(
        df: DataFrame<*>?,
        poseFilePath: String,
        colTypes: Map<String, ColType>
    ): DataFrame<*> {
        val newDf = DataFrame.readCSV(
            poseFilePath,
            colTypes = colTypes,
            delimiter = ',',
            skipLines = PoseEstimationStorageManager.getHeaderLineSize(poseFilePath),
            parserOptions = ParserOptions(Locale.ENGLISH)
        )
        return df?.concat(newDf) ?: newDf
    }

    private fun createMergedDataFrame(): Pair<DataFrame<*>, ArrayList<RecordingMetadataStorage.LabelEntry>> {
        val colTypes =
            PoseEstimationStorageManager.getCSVColumns(Pose.BodyPose).associateWith { colName ->
                when (colName) {
                    "TimeStamp" -> ColType.Long
                    else -> ColType.Double
                }
            }

        var df: DataFrame<*>? = null
        val activities = ArrayList<RecordingMetadataStorage.LabelEntry>()
        val sortedRecordings = recordings.sortedBy { r -> r.metadataStorage.getTimeStarted() }
        sortedRecordings.forEach { recording ->
            val poseFilePath = recording.getPoseSequenceFile().absolutePath
            df = appendPoseFileToDataFrame(df, poseFilePath, colTypes)
            activities.addAll(recording.metadataStorage.getActivities())
        }
        return Pair(df!!, activities)
    }
}