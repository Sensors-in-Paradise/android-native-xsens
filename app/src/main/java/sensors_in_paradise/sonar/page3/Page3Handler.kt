package sensors_in_paradise.sonar.page3

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotPayload
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
// import org.tensorflow.lite.DataType
// import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.ml.XsensTest
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.XSENSArrayList
import java.io.File
import kotlin.collections.ArrayList
import kotlin.math.*

import java.nio.ByteBuffer

class Page3Handler(private val devices: XSENSArrayList) : PageInterface, ConnectionInterface {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PredictionsAdapter
    private lateinit var predictButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var timer: Chronometer

    private val predictions = ArrayList<Prediction>()
    private val rawSensorDataMap = mutableMapOf<String, MutableList<Pair<Long, FloatArray>>>()
    private var sensorDataByteBuffer: ByteBuffer? = null

    private var isRunning = false
    private var numConnectedDevices = 0

    private val numDevices = 5
    private val sizeOfFloat = 4
    private val numQuats = 4
    private val numFreeAccs = 3
    private val dataVectorSize = 180

    val dataLineByteSize = sizeOfFloat * (numQuats + numFreeAccs) * numDevices
    val dataLineFloatSize = (numQuats + numFreeAccs) * numDevices
    var numDataLines = 0

    val sensorTagMap = mapOf(
        "LF" to "D4:22:CD:00:06:7B",
        "LW" to "D4:22:CD:00:06:89",
        "ST" to "D4:22:CD:00:06:7F",
        "RW" to "D4:22:CD:00:06:7D",
        "RF" to "D4:22:CD:00:06:72",
    )

    private fun toggleButtons() {
        startButton.isEnabled = !(startButton.isEnabled)
        stopButton.isEnabled = !(stopButton.isEnabled)
    }

    private fun clearBuffers() {
        for ((_, deviceDataList) in rawSensorDataMap) {
            deviceDataList.clear()
        }
        sensorDataByteBuffer = null
    }

    private fun createByteBuffer() {
        // TODO deal with empty lines of Data Collection
        val minDataLines = rawSensorDataMap.minOfOrNull { it.value.size }
        if (minDataLines == null) return

        for ((k, v) in rawSensorDataMap) {
            Log.d("SensorLists", v.size.toString())
        }

        if (minDataLines < dataVectorSize) {
            Toast.makeText(context, "Not enough data collected!", Toast.LENGTH_SHORT).show()
            return
        }

        numDataLines = dataVectorSize

        var floatArray = FloatArray(0)
        for (row in 0..numDataLines - 1) {
            var lineFloatArray = FloatArray(0)
            for ((_, deviceDataList) in rawSensorDataMap) {
                lineFloatArray = lineFloatArray + deviceDataList[row].second

            }
            var logString = ""
            for (value in lineFloatArray) {
                logString = logString + value.toString() + " "
            }
            Log.d("FloatArray", logString)

            floatArray = floatArray + lineFloatArray
        }

        sensorDataByteBuffer = ByteBuffer.allocate(numDataLines * dataLineByteSize)
        sensorDataByteBuffer?.position(0)
        for (value in floatArray) {
            sensorDataByteBuffer?.putFloat(value)
        }
    }

    private fun stopDataCollection() {
        timer.stop()
        for (device in devices.getConnected()) {
            device.stopMeasuring()
        }

        createByteBuffer()

        toggleButtons()
        isRunning = false
    }

    private fun readBufferFile() : TensorBuffer{
        var data = Page3Handler::class.java.getResource("/squats_raw.raw").readBytes()
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 180, 35), DataType.FLOAT32)
        inputFeature0.loadBuffer(ByteBuffer.wrap(data))
        return inputFeature0
    }

    override fun activityCreated(activity: Activity) {

        this.activity = activity
        this.context = activity

        // Inititalising prediction RV
        recyclerView = activity.findViewById(R.id.rv_prediction)
        val prediction1 = Prediction("Squats", "90%")
        val prediction2 = Prediction("Running", "75%")

        predictions.add(prediction1)
        predictions.add(prediction2)
        adapter = PredictionsAdapter(predictions)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        // Initialising data array
        for ((_, address) in sensorTagMap) {
            rawSensorDataMap.put(address, mutableListOf<Pair<Long, FloatArray>>())
        }

        // Buttons and Timer
        timer = activity.findViewById(R.id.timer_predict_predict)
        startButton = activity.findViewById(R.id.button_start_predict)
        stopButton = activity.findViewById(R.id.button_stop_predict)
        stopButton.isEnabled = false

        startButton.setOnClickListener {
            if (numConnectedDevices >= numDevices) {

                clearBuffers();

                for (device in devices.getConnected()) {
                    device.measurementMode = XsensDotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION
                    device.startMeasuring()
                }
                timer.base = SystemClock.elapsedRealtime()
                timer.start()

                toggleButtons()
                isRunning = true
            } else {
                Toast.makeText(context, "Not enough devices connected!", Toast.LENGTH_SHORT).show()
            }
        }

        stopButton.setOnClickListener {
            stopDataCollection()
        }

        predictButton = activity.findViewById(R.id.button_predict_predict)
        predictButton.setOnClickListener {
            if (sensorDataByteBuffer != null) {
                // get data and model
                val model = XsensTest.newInstance(context)
                // Creates inputs for reference.
                // ----> dimensions: amount, numDataLines * dataLineFloatSize
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, dataVectorSize, dataLineFloatSize), DataType.FLOAT32)
                // TODO: add the following line if input is ready
                inputFeature0.loadBuffer(sensorDataByteBuffer!!)
                // Runs model inference and gets result.
                val inputFeature1 = readBufferFile()

                // without this line (NaN, NaaN) as output, with it always (0.53, 0.46)
                // inputFeature0.buffer.position(25200)

                val outputs = model.process(inputFeature0)

                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                // Releases model resources if no longer used.
                model.close()
                //this is our Output, first Value is for walking, second for squats
                val test = outputFeature0.floatArray
                // TODO: make result visible on the screen!

                Log.d("Prediction", test[0].toString())
                Log.d("Prediction", test[1].toString())

                Toast.makeText(context, test[0].toString() + "    " + test[1].toString(), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please measure an activity first!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {

        numConnectedDevices = devices.getConnected().size

        if (isRunning && numConnectedDevices < numDevices) {
            stopDataCollection()
            Toast.makeText(context, "Connection to device(s) lost!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalize(dataArray: FloatArray, minArray: DoubleArray, maxArray: DoubleArray): FloatArray {
        val numElements = numQuats + numFreeAccs
        val normalizedArray = FloatArray(numElements)

        val lowerBound = 0.0001
        val upperBound = 0.999
        for (i in 0..numElements - 1) {
            val rawNormalize = (dataArray[i].toDouble() - minArray[i]) / (maxArray[i] - minArray[i])
            val clippedNormalize = max(min(upperBound, rawNormalize), lowerBound)

            normalizedArray[i] = clippedNormalize.toFloat()
        }
        return normalizedArray
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {

        val timeStamp: Long = xsensDotData.getSampleTimeFine()
        val quat: FloatArray = xsensDotData.getQuat()
        val freeAcc: FloatArray = xsensDotData.getFreeAcc()

        var normalizedFloatArray = FloatArray(0)
        when (deviceAddress) {
            sensorTagMap["LF"] -> normalizedFloatArray = normalize((quat + freeAcc), doubleArrayOf(-0.8126836   , -0.79424906  , -0.7957623   , -0.8094078  , -31.278593  , -32.166283   , -18.486694 ), doubleArrayOf(0.8145418, 0.79727143  , 0.81989765  , 0.8027102  , 28.956848   , 30.199568  , 22.692505 ))
            sensorTagMap["LW"] -> normalizedFloatArray = normalize((quat + freeAcc), doubleArrayOf( -0.8398707   , -0.8926556   , -0.9343553  , -0.9552342  , -11.258037  , -10.1190405   , -8.37381), doubleArrayOf( 0.7309214   , 0.9186623   , 0.97258735  , 0.9084077  , 10.640987  , 11.26736    , 12.947172 ))
            sensorTagMap["ST"] -> normalizedFloatArray = normalize((quat + freeAcc), doubleArrayOf(-0.87042844 , -0.6713179   , -0.6706054   , -0.80093706 , -20.164385  , -20.21316      , -8.670398  ), doubleArrayOf( 0.87503606  , 0.686213    , 0.67588365  , 0.8398282  , 15.221635   , 13.93141    , 11.752216 ))
            sensorTagMap["RW"] -> normalizedFloatArray = normalize((quat + freeAcc), doubleArrayOf(-0.9208972   , -0.8918428   , -0.9212201   , -0.9103423, -14.090326   , -14.179557    , -11.573973 ), doubleArrayOf(0.93993384  , 0.888225    , 0.9099328  , 0.9181471  , 14.901558   , 11.34146    , 15.649994  ))
            sensorTagMap["RF"] -> normalizedFloatArray = normalize((quat + freeAcc), doubleArrayOf(-0.8756618   , -0.85241073 , -0.8467437   , -0.8629473  , -31.345306   , -31.825573   , -16.296654), doubleArrayOf( 0.8837259   , 0.98513246  , 0.9278882   , 0.8547427  , 31.27872    , 30.43604    , 20.4301 ))
            else -> { // Note the block
                Toast.makeText(context, "Unknown Device!!!", Toast.LENGTH_SHORT).show()
            }
        }

        rawSensorDataMap[deviceAddress]?.add(Pair(timeStamp, normalizedFloatArray))
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // TODO("Not yet implemented")
    }
}
