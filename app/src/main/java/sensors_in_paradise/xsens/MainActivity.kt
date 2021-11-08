package sensors_in_paradise.xsens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.XsensDotSdk
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotDeviceCallback
import com.xsens.dot.android.sdk.interfaces.XsensDotScannerCallback
import com.xsens.dot.android.sdk.models.FilterProfileInfo
import com.xsens.dot.android.sdk.utils.XsensDotScanner
import java.security.Permission
import java.util.*

class MainActivity : AppCompatActivity(), XsensDotDeviceCallback, XsensDotScannerCallback {
    private lateinit var tv: TextView
    private lateinit var rv: RecyclerView
    private lateinit var sensorAdapter: SensorAdapter
    private val scannedDevices = ArrayList<BluetoothDevice>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv_center_acitivity_main)
        rv =  findViewById(R.id.rv_bluetoothDevices_activity_main)
        XsensDotSdk.setDebugEnabled(true)
        initXsScanner()
        val scanStarted = mXsScanner!!.startScan()

        tv.text = "Scan started: $scanStarted"

        val intent = Intent(this, CaptureActivity::class.java)
        findViewById<Button>(R.id.btn_continue_activity_main).setOnClickListener {
            startActivity(intent)
        }



        sensorAdapter = SensorAdapter(scannedDevices)
        rv.adapter = sensorAdapter





        var permissions = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions =  permissions and isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions =  permissions and isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions =  permissions and isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        Toast.makeText(this, "Necessary permissions granted: $permissions",Toast.LENGTH_LONG).show()

    }
    private fun isPermissionGranted(permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(this, permission) ==PackageManager.PERMISSION_GRANTED)
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

        if(!scannedDevices.contains(device)){
            scannedDevices.add(device)

            sensorAdapter.notifyItemInserted(scannedDevices.size-1)
            //Toast.makeText(this, "Device discovered",Toast.LENGTH_LONG).show()

        }
        tv.visibility = View.INVISIBLE;

    }
}