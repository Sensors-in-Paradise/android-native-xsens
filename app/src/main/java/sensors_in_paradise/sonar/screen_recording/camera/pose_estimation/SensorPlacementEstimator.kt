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
    //private val numRecordingsTV: TextView, TODO
    val onSelectedRecordingsChanged: ((List<Recording>) -> Unit)
) {
    val recordings = mutableListOf<Recording>()
    private var numPositions = 1

    companion object {
        val POSITIONS = SensorPlacementDialog.POSITIONS_MAP.keys
        const val FRAME_LENGTH = 500
    }

    init {
        estimatePlacementButton.setOnClickListener {
            val dialog = SensorPlacementDialog(context, mapOf())
            val scores = tryEstimateSensorPlacements()
            scores?.let { dialog.updateScores(it) }
        }
        lessPositionsButton.setOnClickListener {
            // TODO
        }
        morePositionsButton.setOnClickListener {
            // TODO
        }
        numPositionsTV.text = "$numPositions Positions"
        // numRecordingsTV.text = "1" TODO
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
        // numRecordingsTV.text = "${recordings.size}" TODO
    }

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

    private fun tryEstimateSensorPlacements(): Map<String, Float>? {
        val recording = recordings[0]// TODO

        try {
            // TODO
            // assert min 2 activities with min 2 over 1 min
        } catch (e: Exception) {
            Toast.makeText( // TODO
                context,
                e.message,
                Toast.LENGTH_LONG
            ).show()
            return null
        }
        return estimateRecording(recording)
    }

    private fun estimateRecording(recording: Recording): Map<String, Float>? {
        try {
            val poseFilePath = recording.getPoseSequenceFile().absolutePath
            var df = createDataFrame(poseFilePath)

            df = mergeColumns(df)

            val activities = recording.metadataStorage.getActivities()
            df = appendActivities(df, activities)

            val activityGroups = df.groupBy("Activity")
            val activityFrames = activityGroups.keys.values().mapIndexedNotNull { index, activity ->
                val frame = activityGroups.groups[index]
                if (frame.size().nrow >= FRAME_LENGTH) {
                    activity.toString() to frame
                } else null
            }.toMap()

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
                "Estimation of Sensor Placement Failed:\n ${e.toString()}",
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

    private fun createDataFrame(poseFilePath: String): DataFrame<*> {
        val colTypes =
            PoseEstimationStorageManager.getCSVColumns(Pose.BodyPose).associateWith { colName ->
                when (colName) {
                    "TimeStamp" -> ColType.Long
                    else -> ColType.Double
                }
            }
        return DataFrame.readCSV(
            poseFilePath,
            colTypes = colTypes,
            delimiter = ',',
            skipLines = PoseEstimationStorageManager.getHeaderLineSize(poseFilePath),
            parserOptions = ParserOptions(Locale.ENGLISH)
        )
    }
}