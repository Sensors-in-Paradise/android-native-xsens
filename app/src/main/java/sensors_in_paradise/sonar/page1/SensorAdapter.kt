package sensors_in_paradise.sonar.page1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.models.XsensDotDevice
import sensors_in_paradise.sonar.R
import sensors_in_paradise.xsens.page1.XSENSArrayList

class SensorAdapter(
    private val devices: XSENSArrayList,
    private val connectionCallbackUI: UIDeviceConnectionInterface
) :
    RecyclerView.Adapter<SensorAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tv_name_sensorDevice)
        val detailsTextView: TextView = view.findViewById(R.id.tv_details_sensorDevice)
        val button: Button = view.findViewById(R.id.switch_connect_sensorDevice)
        val flipper: ViewFlipper = view.findViewById(R.id.flipper_sensorDevice)
        val batteryPB: ProgressBar = view.findViewById(R.id.pb_battery_sensorDevice)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sensor_device, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        val device = devices[position]
        viewHolder.nameTextView.text = "${device.name} ${device.tag}"

        viewHolder.button.setOnClickListener {
            val isConnectedState = (viewHolder.button.text == "Connect")
            connectionCallbackUI.onConnectionUpdateRequested(device, isConnectedState)
        }
        val isConnected = (device.connectionState == XsensDotDevice.CONN_STATE_CONNECTED)
        val isConnecting = (device.connectionState == XsensDotDevice.CONN_STATE_CONNECTING)
        val isReconnecting = (device.connectionState == XsensDotDevice.CONN_STATE_RECONNECTING)
        val isConnectingOrReconnecting = isConnecting or isReconnecting
        viewHolder.flipper.displayedChild = if (isConnectingOrReconnecting) 1 else 0
        viewHolder.button.text = if (isConnected) "Disconnect" else "Connect"
        viewHolder.detailsTextView.text = if (isConnected) "Connected" else "Disconnected"
        viewHolder.batteryPB.progress = if (isConnected) device.batteryPercentage else 0
        viewHolder.batteryPB.visibility = if (isConnected) View.VISIBLE else View.INVISIBLE
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return devices.size
    }

    fun notifyItemChanged(address: String) {
        val index = devices.indexOf(address)
        if (index != -1) {
            notifyItemChanged(index)
        }
    }
}
