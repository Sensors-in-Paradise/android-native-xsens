package sensors_in_paradise.sonar

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.xsens.dot.android.sdk.events.XsensDotData
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.page1.Page1Handler
import sensors_in_paradise.sonar.page1.XSENSArrayList
import sensors_in_paradise.sonar.page2.Page2Handler

class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener, ConnectionInterface {

    private lateinit var flipper: ViewFlipper
    private lateinit var tabLayout: TabLayout

    private val pageHandlers = ArrayList<PageInterface>()

    private val scannedDevices = XSENSArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flipper = findViewById(R.id.flipper_activity_main)
        tabLayout = findViewById(R.id.tab_layout_activity_main)

        initClickListeners()

        val page2 = Page2Handler(scannedDevices)
        pageHandlers.add(Page1Handler(scannedDevices, page2))
        pageHandlers.add(page2)

        for (handler in pageHandlers) {
            handler.activityCreated(this)
        }
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.colorPrimary)))
    }

    override fun onResume() {
        super.onResume()
        for (handler in pageHandlers) {
            handler.activityResumed()
        }
    }
    private fun initClickListeners() {
        tabLayout.addOnTabSelectedListener(this)
    }
    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab != null) {
            flipper.displayedChild = tab.position
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // TODO("Not yet implemented")
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // TODO("Not yet implemented")
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        // TODO("Not yet implemented")
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        // TODO("Not yet implemented")
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {
        // TODO("Not yet implemented")
    }
}
