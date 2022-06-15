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

class SensorPlacementDialog(
    val context: Context,
    ) {
    private var imageView: ImageView
    private var textView: TextView
    private var scores = mapOf<String, Float>()

    init {
        val builder = AlertDialog.Builder(context)
        val root = LayoutInflater.from(context).inflate(R.layout.sensor_placement_dialog, null)
        imageView = root.findViewById(R.id.textureView_skeleton_sensorPlacementDialog)
        textView = root.findViewById(R.id.textView_ranking_sensorPlacementDialog)
        builder.setView(root)

        builder.setPositiveButton("ok", null)

        builder.setOnDismissListener {
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
        normalizeScores()

        textView.text = getRankingString()
        drawScores()
    }

    private fun drawScores() {
        val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.RGB_565)
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
        imageView.setImageBitmap(bitmap)
    }

    private fun normalizeScores() {
        if (scores.isEmpty()) {
            return
        }
        val upperBound = max(scores.values)
        val lowerBound = min(scores.values)
        scores = scores.mapValues { (_, score) ->
            if (scores.size == 1) 1f
            else (score - lowerBound) / (upperBound - lowerBound)
        }
    }

    private fun getPositionColors(): List<Int?> {
        return POSITIONS_MAP.keys.map { position ->
            scores.getOrDefault(position, null)?.let { normScore ->
                ColorUtils.blendARGB(
                    context.getColor(R.color.backgroundContrast),
                    context.getColor(R.color.colorSecondary),
                    normScore
                )
            }
        }
    }

    private fun getRankingString(): String {
        val rankedScores = scores.asIterable().sortedBy { -it.value }

        return rankedScores.joinToString(
            "\n",
            limit = 99,
            transform = { (position, score) ->
                val name = position
                    .replace('_', ' ')
                    .lowercase()
                    .replaceFirstChar { it.titlecase() }
                val score = (score * 100f).toInt()
                var padding = ""
                if (score < 100) padding += "  "
                if (score < 10) padding += "  "
                "$padding$score%  -  $name"
            }
        )
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

        private val POSE_POINTS = { listOf(POSITIONS_MAP.values.map { PointF(it.x, it.y) }) }

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