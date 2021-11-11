package sensors_in_paradise.xsens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.XsensDotSdk
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotDeviceCallback
import com.xsens.dot.android.sdk.interfaces.XsensDotScannerCallback
import com.xsens.dot.android.sdk.models.FilterProfileInfo
import com.xsens.dot.android.sdk.utils.XsensDotScanner
import sensors_in_paradise.xsens.devices.DeviceConnectionInterface
import sensors_in_paradise.xsens.devices.SensorAdapter
import sensors_in_paradise.xsens.devices.StatefulBluetoothDevice
import java.util.*
import android.bluetooth.BluetoothAdapter
import android.widget.*


class MainActivity : AppCompatActivity(), XsensDotDeviceCallback, XsensDotScannerCallback,
    DeviceConnectionInterface {
    private lateinit var tv: TextView
    private lateinit var pb: ProgressBar
    private lateinit var rv: RecyclerView
    private lateinit var linearLayout_center: LinearLayout
    private lateinit var sensorAdapter: SensorAdapter
    private val scannedDevices = ArrayList<StatefulBluetoothDevice>()
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv = findViewById(R.id.tv_center_acitivity_main)
        rv = findViewById(R.id.rv_bluetoothDevices_activity_main)
        pb = findViewById(R.id.pb_activity_main)
        linearLayout_center = findViewById(R.id.linearLayout_center_activity_main)
        sensorAdapter = SensorAdapter(scannedDevices, this)
        rv.adapter = sensorAdapter

        initClickListeners()
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            // Permissions granted, starting scan
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
                tv.text = "Scan started: false\nDevice does not support bluetooth"
                pb.visibility = View.INVISIBLE
                tv.visibility = View.VISIBLE
            } else if (!mBluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled :)
                tv.text = "Scan started: false\nPlease enable bluetooth"
                pb.visibility = View.INVISIBLE
                tv.visibility = View.VISIBLE
            } else {
                // Bluetooth is enabled
                XsensDotSdk.setDebugEnabled(true)
                initXsScanner()
                val scanStarted = mXsScanner!!.startScan()
                tv.text = "Scan started: $scanStarted"
                pb.visibility = View.VISIBLE
            }
        }
    }
    private fun checkPermissions(): Boolean {
        val requiredPermissions = getRequiredButUngrantedPermissions()
        if (requiredPermissions.size > 0) {
            // Not all permissions granted
            val s = requiredPermissions.joinToString(separator = ", \n")
            tv.text = "Scan started: false\nThe following permissions need to be granted:\n$s"
            return false
        }
        return true
    }

    private fun initClickListeners() {
        findViewById<Button>(R.id.btn_continue_activity_main).setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getRequiredButUngrantedPermissions(): ArrayList<String> {
        val result = ArrayList<String>()
        for (permission in REQUIRED_PERMISSIONS) {
            if (!isPermissionGranted(permission)) {
                result.add(permission)
            }
        }
        return result
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED)
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
    override fun onXsensDotGetFilterProfileInfo(
        s: String,
        arrayList: ArrayList<FilterProfileInfo>
    ) {
    }

    override fun onSyncStatusUpdate(s: String, b: Boolean) {

    }

    override fun onXsensDotScanned(device: BluetoothDevice, i: Int) {
        //TODO("Move logic into new  arraylist class")
        var alreadyAdded = false

        for(device2 in scannedDevices){
            if(device2.device == device){
                alreadyAdded = true
            }
        }
        if (!alreadyAdded) {
            scannedDevices.add(StatefulBluetoothDevice(device))
            sensorAdapter.notifyItemInserted(scannedDevices.size - 1)
        }

        linearLayout_center.visibility = View.INVISIBLE
    }

    override fun onConnectionUpdateRequested(
        statefulDevice: StatefulBluetoothDevice,
        wantsConnection: Boolean
    ) {
        Toast.makeText(
            this,
            statefulDevice.device.address + " wants a connection: " + wantsConnection,
            Toast.LENGTH_LONG
        ).show()
    }
}
