package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import android.content.Context
import android.widget.Toast
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

class SensorPlacementEstimator() {

    companion object {
        val POSITIONS = listOf(
            "HEAD",
            "LEFT_SHOULDER",
            "RIGHT_SHOULDER",
            "LEFT_ELBOW",
            "RIGHT_ELBOW",
            "LEFT_WRIST",
            "RIGHT_WRIST",
            "HIP",
            "LEFT_KNEE",
            "RIGHT_KNEE",
            "LEFT_ANKLE",
            "RIGHT_ANKLE"
        )
        const val FRAME_LENGTH = 500
    }

    fun estimateRecording(context: Context, recording: Recording) {
        try {
            if (!recording.hasPoseSequenceRecording()) {
                throw Exception("No Body Pose Sequence available.")
            }

            val poseFilePath = recording.getPoseSequenceFile().absolutePath
            if (PoseEstimationStorageManager.getPoseTypeFromCSV(context, poseFilePath)
                != Pose.BodyPose
            ) {
                throw Exception("Pose Sequence not compatible. Sequence has to be of type 'Body Pose'")
            }

            // TODO
            // assert min 2 activities with min 2 over 1 min

            var df = createDataFrame(poseFilePath)

            df = mergeColumns(df)

            val activities = recording.metadataStorage.getActivities()
            df = appendActivities(df, activites)

            val activityFrames = mutableListOf<DataFrame<*>>()
            activities.forEach { activity ->
                val frame = df.groupBy("Activity").toDataFrame() // TODO
                if (frame.size().nrow >= FRAME_LENGTH) {
                    activityFrames.add(frame)
                }
            }

            POSITIONS.forEach {

            }

            // dataframe per activity per sensor

            // for l

        } catch (e: Exception) {
            Toast.makeText(
                context,
                e.message,
                Toast.LENGTH_LONG
            ).show()
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
            //colTypes = colTypes,
            ',',
            skipLines = PoseEstimationStorageManager.getHeaderLineSize(poseFilePath),
            parserOptions = ParserOptions(Locale.ENGLISH)
        )
    }
}