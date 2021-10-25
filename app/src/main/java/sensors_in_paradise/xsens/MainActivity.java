package sensors_in_paradise.xsens;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.xsens.dot.android.sdk.XsensDotSdk;
import com.xsens.dot.android.sdk.events.XsensDotData;
import com.xsens.dot.android.sdk.interfaces.XsensDotDeviceCallback;
import com.xsens.dot.android.sdk.interfaces.XsensDotScannerCallback;
import com.xsens.dot.android.sdk.models.FilterProfileInfo;
import com.xsens.dot.android.sdk.utils.XsensDotScanner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements XsensDotDeviceCallback, XsensDotScannerCallback {
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.textView);

        XsensDotSdk.setDebugEnabled(true);
        initXsScanner();
        boolean scanStarted = mXsScanner.startScan();

        Log.println(Log.INFO,"XSENS", "");
        tv.setText("Scan started: "+scanStarted);

    }
    private XsensDotScanner mXsScanner;
    private void initXsScanner() {
        mXsScanner = new XsensDotScanner(this, this);
        mXsScanner.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
    }

    @Override
    public void onXsensDotConnectionChanged(String s, int i) {

    }

    @Override
    public void onXsensDotServicesDiscovered(String s, int i) {

    }

    @Override
    public void onXsensDotFirmwareVersionRead(String s, String s1) {

    }

    @Override
    public void onXsensDotTagChanged(String s, String s1) {

    }

    @Override
    public void onXsensDotBatteryChanged(String s, int i, int i1) {

    }

    @Override
    public void onXsensDotDataChanged(String s, XsensDotData xsensDotData) {

    }

    @Override
    public void onXsensDotInitDone(String s) {

    }

    @Override
    public void onXsensDotButtonClicked(String s, long l) {

    }

    @Override
    public void onXsensDotPowerSavingTriggered(String s) {

    }

    @Override
    public void onReadRemoteRssi(String s, int i) {

    }

    @Override
    public void onXsensDotOutputRateUpdate(String s, int i) {

    }

    @Override
    public void onXsensDotFilterProfileUpdate(String s, int i) {

    }

    @Override
    public void onXsensDotGetFilterProfileInfo(String s, ArrayList<FilterProfileInfo> arrayList) {

    }

    @Override
    public void onSyncStatusUpdate(String s, boolean b) {

    }

    @Override
    public void onXsensDotScanned(BluetoothDevice device, int i) {
        String name = device.getName();
        String address = device.getAddress();

        tv.setText("Name: "+name+" address:"+address);
        Log.println(Log.INFO,"XSENS", "Device discovered: "+name+" ---> "+address);
    }
}
