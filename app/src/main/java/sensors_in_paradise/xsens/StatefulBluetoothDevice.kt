package sensors_in_paradise.xsens

import android.bluetooth.BluetoothDevice

class StatefulBluetoothDevice( val device: BluetoothDevice) {
    public var connected = false
    public override fun equals(other: Any?): Boolean {
        if(other is BluetoothDevice){
            return device == other
        }
        return super.equals(other)
    }
}