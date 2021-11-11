package sensors_in_paradise.xsens.page1

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xsens.dot.android.sdk.models.XsensDotDevice
import sensors_in_paradise.xsens.R
import sensors_in_paradise.xsens.StatefulBluetoothDevice

class SensorAdapter(
    private val devices: ArrayList<XsensDotDevice>,
    private val connectionCallbackUI: UIDeviceConnectionInterface
) :
    RecyclerView.Adapter<SensorAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tv_name_sensorDevice)
        val switch: Switch = view.findViewById(R.id.switch_connect_sensorDevice)

        init {


        }
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

        viewHolder.textView.text = device.name + " " + device.name
        viewHolder.switch.isChecked = device.connectionState == XsensDotDevice.CONN_STATE_CONNECTED
        viewHolder.switch.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            connectionCallbackUI.onConnectionUpdateRequested(device, b)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return devices.size
    }
}
