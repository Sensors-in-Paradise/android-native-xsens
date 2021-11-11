package sensors_in_paradise.xsens.page1

import sensors_in_paradise.xsens.StatefulBluetoothDevice

interface DeviceConnectionInterface {
    fun onConnectionUpdateRequested(statefulDevice: StatefulBluetoothDevice, wantsConnection: Boolean)
}