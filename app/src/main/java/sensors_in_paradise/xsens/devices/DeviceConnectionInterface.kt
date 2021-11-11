package sensors_in_paradise.xsens.devices

interface DeviceConnectionInterface {
    fun onConnectionUpdateRequested(statefulDevice: StatefulBluetoothDevice, wantsConnection: Boolean)
}