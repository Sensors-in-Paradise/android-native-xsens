package sensors_in_paradise.sonar.custom_views.stickman

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.custom_views.stickman.object3d.Sensor3D

class StickmanDialog(context: Context) : OnSeekBarChangeListener {
    private var dialog: AlertDialog
    private val sensor = Sensor3D().apply {
        drawVertexPositionsForDebugging = true
        // translate(0f, 0.5f, 0f, false)
        // updateDefaultState()
    }
    private var xRotationSlider: SeekBar
    private var yRotationSlider: SeekBar
    private var zRotationSlider: SeekBar
    private var xTranslationSlider: SeekBar
    private var yTranslationSlider: SeekBar
    private var zTranslationSlider: SeekBar
    private var xScaleSlider: SeekBar
    private var yScaleSlider: SeekBar
    private var zScaleSlider: SeekBar
    private val tagTvMap = mutableMapOf<String, TextView>()

    init {

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Stickman")

        val root = LayoutInflater.from(context).inflate(R.layout.stickman_dialog, null)
        val stickmanView = root.findViewById<Render3DView>(R.id.stickmanView)
        xRotationSlider = root.findViewById(R.id.slider_xEuler_stickmanDialog)
        yRotationSlider = root.findViewById(R.id.slider_yEuler_stickmanDialog)
        zRotationSlider = root.findViewById(R.id.slider_zEuler_stickmanDialog)
        tagTvMap["euler_x"] = root.findViewById(R.id.tv_eulerX_stickmanDialog)
        tagTvMap["euler_y"] = root.findViewById(R.id.tv_eulerY_stickmanDialog)
        tagTvMap["euler_z"] = root.findViewById(R.id.tv_eulerZ_stickmanDialog)

        xScaleSlider = root.findViewById(R.id.slider_xScale_stickmanDialog)
        yScaleSlider = root.findViewById(R.id.slider_yScale_stickmanDialog)
        zScaleSlider = root.findViewById(R.id.slider_zScale_stickmanDialog)
        tagTvMap["scale_x"] = root.findViewById(R.id.tv_scaleX_stickmanDialog)
        tagTvMap["scale_y"] = root.findViewById(R.id.tv_scaleY_stickmanDialog)
        tagTvMap["scale_z"] = root.findViewById(R.id.tv_scaleZ_stickmanDialog)

        xTranslationSlider = root.findViewById(R.id.slider_xTranslate_stickmanDialog)
        yTranslationSlider = root.findViewById(R.id.slider_yTranslate_stickmanDialog)
        zTranslationSlider = root.findViewById(R.id.slider_zTranslate_stickmanDialog)
        tagTvMap["translate_x"] = root.findViewById(R.id.tv_translationX_stickmanDialog)
        tagTvMap["translate_y"] = root.findViewById(R.id.tv_translationY_stickmanDialog)
        tagTvMap["translate_z"] = root.findViewById(R.id.tv_translationZ_stickmanDialog)

        stickmanView.apply {
            addObject3D(sensor)
            // addObject3D(coordinateSystem3D)
        }

        stickmanView.camera.center.apply {
            x = 0f
            y = 0.5f
            z = 0f
        }
        stickmanView.camera.eye.apply {
            x = 0f
            y = 1f
            z = -2f
        }
        stickmanView.camera.up.apply {
            x = 0f
            y = 1f
            z = 0f
        }
        stickmanView.camera.notifyCameraChanged()
        stickmanView.onObjectChanged()
        stickmanView.enableYRotation = true
        stickmanView.showFPS = true

        xRotationSlider.setOnSeekBarChangeListener(this)
        yRotationSlider.setOnSeekBarChangeListener(this)
        zRotationSlider.setOnSeekBarChangeListener(this)
        xTranslationSlider.setOnSeekBarChangeListener(this)
        yTranslationSlider.setOnSeekBarChangeListener(this)
        zTranslationSlider.setOnSeekBarChangeListener(this)
        xScaleSlider.setOnSeekBarChangeListener(this)
        yScaleSlider.setOnSeekBarChangeListener(this)
        zScaleSlider.setOnSeekBarChangeListener(this)

        builder.setView(root)
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        sensor.resetToDefaultState(shouldNotifyThatObjectChanged = false)
        if (seekBar != null) {
            val tag = seekBar.tag.toString()
            val tv = tagTvMap[tag]!!
            val prefix = (if (tv.text.contains(":")) tv.text.substring(0, tv.text.indexOf(':')) else tv.text).toString()
            tv.text = "$prefix: $progress"
            sensor.translate(
                xTranslationSlider.progress.toFloat(),
                yTranslationSlider.progress.toFloat(),
                zTranslationSlider.progress.toFloat()
            )
            sensor.scale(
                xScaleSlider.progress.toFloat(),
                yScaleSlider.progress.toFloat(),
                zScaleSlider.progress.toFloat()
            )
            sensor.rotateEuler(
                xRotationSlider.progress.toFloat(),
                yRotationSlider.progress.toFloat(),
                zRotationSlider.progress.toFloat(),
            )
            when {
                tag.startsWith("translate") -> {
                    Log.d("StickmanDialog", "Translating object")
                }
                tag.startsWith("scale") -> {
                    Log.d("StickmanDialog", "Scaling object")
                }
                tag.startsWith("euler") -> {
                    Log.d("StickmanDialog", "Rotating object")
                }
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}
