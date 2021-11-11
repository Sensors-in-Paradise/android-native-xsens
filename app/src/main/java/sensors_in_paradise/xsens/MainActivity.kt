package sensors_in_paradise.xsens

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xsens.dot.android.sdk.XsensDotSdk
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotDeviceCallback
import com.xsens.dot.android.sdk.interfaces.XsensDotScannerCallback
import com.xsens.dot.android.sdk.models.FilterProfileInfo
import com.xsens.dot.android.sdk.utils.XsensDotScanner
import java.util.*

class MainActivity : AppCompatActivity(), XsensDotDeviceCallback, XsensDotScannerCallback {
    private lateinit var tv: TextView

    private val scannedDevices = ArrayList<BluetoothDevice>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv)
        XsensDotSdk.setDebugEnabled(true)
        initXsScanner()
        val scanStarted = mXsScanner!!.startScan()
        Log.println(Log.INFO, "XSENS", "")
        tv.text = "Scan started: $scanStarted"

        val intent = Intent(this, CaptureActivity::class.java)

        findViewById<Button>(R.id.btn_continue_activity_main).setOnClickListener {
        }
    }

    private var mXsScanner: XsensDotScanner? = null
    private fun initXsScanner() {
        mXsScanner = XsensDotScanner(this, this)
        mXsScanner!!.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
    }

    override fun onXsensDotConnectionChanged(s: String, i: Int) {}
    override fun onXsensDotServicesDiscovered(s: String, i: Int) {}
    override fun onXsensDotFirmwareVersionRead(s: String, s1: String) {}
    override fun onXsensDotTagChanged(s: String, s1: String) {}
    override fun onXsensDotBatteryChanged(s: String, i: Int, i1: Int) {}
    override fun onXsensDotDataChanged(s: String, xsensDotData: XsensDotData) {}
    override fun onXsensDotInitDone(s: String) {}
    override fun onXsensDotButtonClicked(s: String, l: Long) {}
    override fun onXsensDotPowerSavingTriggered(s: String) {}
    override fun onReadRemoteRssi(s: String, i: Int) {}
    override fun onXsensDotOutputRateUpdate(s: String, i: Int) {}
    override fun onXsensDotFilterProfileUpdate(s: String, i: Int) {}
    override fun onXsensDotGetFilterProfileInfo(s: String, arrayList: ArrayList<FilterProfileInfo>) {}
    override fun onSyncStatusUpdate(s: String, b: Boolean) {}
    override fun onXsensDotScanned(device: BluetoothDevice, i: Int) {
        if (!scannedDevices.contains(device)){
            scannedDevices.add(device)
            val name = device.name
            val address = device.address
            val currentText = tv.text
            tv.text = "$currentText\n Name: $name address:$address"
            Log.println(Log.INFO, "XSENS", "Device discovered: $name ---> $address")
        }
    }
}
