package sensors_in_paradise.xsens

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import com.google.android.material.tabs.TabLayout
import com.xsens.dot.android.sdk.models.XsensDotDevice
import sensors_in_paradise.xsens.page1.ConnectionInterface
import sensors_in_paradise.xsens.page1.Page1Handler

class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener, ConnectionInterface {

    private lateinit var flipper: ViewFlipper
    private lateinit var tabLayout: TabLayout

    private val pageHandlers = ArrayList<PageInterface>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        flipper = findViewById(R.id.flipper_activity_main)
        tabLayout = findViewById(R.id.tab_layout_activity_main)

        initClickListeners()

        pageHandlers.add(Page1Handler(this))
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
        //TODO("Not yet implemented")
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        //TODO("Not yet implemented")
    }

    override fun onConnectedDevicesChanged(devices: ArrayList<XsensDotDevice>) {
        //TODO("Not yet implemented")
    }
}
