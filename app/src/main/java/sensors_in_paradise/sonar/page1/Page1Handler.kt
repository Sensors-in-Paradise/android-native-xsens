package sensors_in_paradise.sonar.page1

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.XsensDotSdk
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.interfaces.XsensDotDeviceCallback
import com.xsens.dot.android.sdk.interfaces.XsensDotScannerCallback
import com.xsens.dot.android.sdk.models.FilterProfileInfo
import com.xsens.dot.android.sdk.models.XsensDotDevice
import com.xsens.dot.android.sdk.utils.XsensDotScanner
import sensors_in_paradise.sonar.PageInterface
import sensors_in_paradise.sonar.R

class Page1Handler(private val scannedDevices: XSENSArrayList, private val connectionInterface: ConnectionInterface) :
    XsensDotScannerCallback, XsensDotDeviceCallback, PageInterface,
    UIDeviceConnectionInterface {
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var tv: TextView
    private lateinit var pb: ProgressBar
    private lateinit var rv: RecyclerView
    private lateinit var linearLayoutCenter: LinearLayout
    private lateinit var sensorAdapter: SensorAdapter

    private val _requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onXsensDotConnectionChanged(address: String, state: Int) {
        activity.runOnUiThread {
            connectionInterface.onConnectedDevicesChanged(address,
            state == XsensDotDevice.CONN_STATE_CONNECTED)
        if (state != XsensDotDevice.CONN_STATE_CONNECTED) {
            sensorAdapter.notifyItemChanged(address)
            }
        }
    }
    override fun onXsensDotServicesDiscovered(address: String, status: Int) {
    }
    override fun onXsensDotFirmwareVersionRead(s: String, s1: String) {}
    override fun onXsensDotTagChanged(address: String, tag: String) {
        activity.runOnUiThread {
            sensorAdapter.notifyItemChanged(address)
        }
    }
    override fun onXsensDotBatteryChanged(s: String, i: Int, i1: Int) {}
    override fun onXsensDotDataChanged(address: String, xsensDotData: XsensDotData) {
        connectionInterface.onXsensDotDataChanged(address, xsensDotData)
    }
    override fun onXsensDotInitDone(address: String) {
        activity.runOnUiThread {
            sensorAdapter.notifyItemChanged(address)
        }
    }
    override fun onXsensDotButtonClicked(s: String, l: Long) {}
    override fun onXsensDotPowerSavingTriggered(s: String) {}
    override fun onReadRemoteRssi(s: String, i: Int) {}
    override fun onXsensDotOutputRateUpdate(address: String, outputRate: Int) {
        connectionInterface.onXsensDotOutputRateUpdate(address, outputRate)
    }
    override fun onXsensDotFilterProfileUpdate(s: String, i: Int) {}
    override fun onXsensDotGetFilterProfileInfo(
        s: String,
        arrayList: ArrayList<FilterProfileInfo>
    ) {
    }
    override fun onSyncStatusUpdate(s: String, b: Boolean) {}
    override fun onXsensDotScanned(device: BluetoothDevice, i: Int) {
        if (!scannedDevices.contains(device.address)) {
            scannedDevices.add(XsensDotDevice(context, device, this))
            sensorAdapter.notifyItemInserted(scannedDevices.size - 1)
        }
        linearLayoutCenter.visibility = View.INVISIBLE
    }
    override fun activityCreated(activity: Activity) {
        this.context = activity
        this.activity = activity
        tv = activity.findViewById(R.id.tv_center_acitivity_main)
        rv = activity.findViewById(R.id.rv_bluetoothDevices_activity_main)
        pb = activity.findViewById(R.id.pb_activity_main)
        linearLayoutCenter = activity.findViewById(R.id.linearLayout_center_activity_main)
        sensorAdapter = SensorAdapter(scannedDevices, this)
        rv.adapter = sensorAdapter
    }
    override fun activityResumed() {
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
    private var mXsScanner: XsensDotScanner? = null
    private fun initXsScanner() {
        mXsScanner = XsensDotScanner(context, this)
        mXsScanner!!.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
    }
    override fun onConnectionUpdateRequested(
        device: XsensDotDevice,
        wantsConnection: Boolean
    ) {
       if (wantsConnection) {
           device.connect()
       } else {
           device.disconnect()
       }
        activity.runOnUiThread {
            sensorAdapter.notifyItemChanged(device.address)
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
    private fun getRequiredButUngrantedPermissions(): ArrayList<String> {
        val result = ArrayList<String>()
        for (permission in _requiredPermissions) {
            if (!isPermissionGranted(permission)) {
                result.add(permission)
            }
        }
        return result
    }
    private fun isPermissionGranted(permission: String): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
