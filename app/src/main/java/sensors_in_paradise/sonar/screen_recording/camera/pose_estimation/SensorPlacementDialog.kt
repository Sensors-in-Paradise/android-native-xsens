package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import sensors_in_paradise.sonar.R
import java.util.Collections.max
import java.util.Collections.min

class SensorPlacementDialog(val context: Context, var scores: Map<String, Float>) {
    private var textureView: ImageView
    private var textView: TextView

    init {
        val builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.sensor_placement_dialog, null)
        textureView = root.findViewById(R.id.textureView_skeleton_sensorPlacementDialog)
        textView = root.findViewById(R.id.textView_ranking_sensorPlacementDialog)
        builder.setView(root)

        textView.text = "dfgdgfsdgfdgfs" // TODO

        builder.setPositiveButton(
            "ok", null
        )
//        {
//                TODO clearSelectedRecordings
//            }


        builder.setOnDismissListener {
            //                TODO clearSelectedRecordings
            Log.d("SensorPlacementDialog", "Dialog dismissed")
        }

        // Create the AlertDialog object and return it
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.setCancelable(false)
        }
        dialog.show()
    }

    fun updateScores(newScores: Map<String, Float>) {
        scores = newScores
        drawScores()
    }

    private fun drawScores() {
        val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val poses = POSE_POINTS()
        VisualizationUtils.transformPoints(
            poses,
            bitmap,
            canvas,
            VisualizationUtils.Transformation.PROJECT_ON_CANVAS
        )
        VisualizationUtils.drawSkeleton(
            poses,
            canvas,
            POSE_LINES,
            clearColor = context.getColor(R.color.slightBackgroundContrast),
            circleColors = getPositionColors(),
            lineColor = context.getColor(R.color.backgroundContrast)
        )
        textureView.setImageBitmap(bitmap)
    }

    private fun getPositionColors(): List<Int?> {
        if (scores.isEmpty()) {
            return POSITIONS_MAP.map { null }
        }
        val upperBound = max(scores.values)
        val lowerBound = min(scores.values)
        return POSITIONS_MAP.keys.map { position ->
            scores.getOrDefault(position, null)?.let { score ->
                val relScore = if (scores.size == 1) 1f
                    else (score - lowerBound) / (upperBound - lowerBound) + lowerBound
                ColorUtils.blendARGB(context.getColor(R.color.backgroundContrast), Color.RED, relScore)
            }
        }
    }

    companion object {
        val POSITIONS_MAP = mapOf(
            "HEAD" to PointF(0.5f, 0.15f),
            "RIGHT_SHOULDER" to PointF(0.4f, 0.25f),
            "LEFT_SHOULDER" to PointF(0.6f, 0.25f),
            "RIGHT_ELBOW" to PointF(0.3f, 0.4f),
            "LEFT_ELBOW" to PointF(0.7f, 0.4f),
            "RIGHT_WRIST" to PointF(0.3f, 0.55f),
            "LEFT_WRIST" to PointF(0.7f, 0.55f),
            "HIP" to PointF(0.5f, 0.5f),
            "RIGHT_KNEE" to PointF(0.45f, 0.675f),
            "LEFT_KNEE" to PointF(0.55f, 0.675f),
            "RIGHT_ANKLE" to PointF(0.45f, 0.85f),
            "LEFT_ANKLE" to PointF(0.55f, 0.85f)
        )

        private val POSE_POINTS = { listOf(POSITIONS_MAP.values.map { PointF(it.x, it.y)}) }

        private val POSE_LINES = listOf(
            Pair(0, 1),
            Pair(0, 2),
            Pair(1, 2),
            Pair(1, 3),
            Pair(1, 7),
            Pair(2, 4),
            Pair(2, 7),
            Pair(3, 5),
            Pair(4, 6),
            Pair(7, 8),
            Pair(7, 9),
            Pair(8, 10),
            Pair(9, 11)
        )
    }
}