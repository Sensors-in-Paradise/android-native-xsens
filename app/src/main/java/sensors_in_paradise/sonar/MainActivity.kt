package sensors_in_paradise.sonar

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ViewAnimator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import sensors_in_paradise.sonar.page1.Page1Handler
import sensors_in_paradise.sonar.page2.Page2Handler
import sensors_in_paradise.sonar.page2.RecordingDataManager
import sensors_in_paradise.sonar.page3.Page3Handler
import sensors_in_paradise.sonar.uploader.RecordingsUploaderDialog
import sensors_in_paradise.sonar.uploader.OwnCloudRecordingsUploader
import java.io.File


class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    private lateinit var switcher: ViewAnimator
    private lateinit var tabLayout: TabLayout
    private lateinit var ownCloudUploader: OwnCloudRecordingsUploader
    private lateinit var  recordingsManager :RecordingDataManager

    private val pageHandlers = ArrayList<PageInterface>()

    private val scannedDevices = XSENSArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switcher = findViewById(R.id.switcher_activity_main)
        tabLayout = findViewById(R.id.tab_layout_activity_main)
        recordingsManager = RecordingDataManager(
            GlobalValues.getSensorRecordingsBaseDir(this)
        )

        initClickListeners()
        val page1Handler = Page1Handler(scannedDevices)
        pageHandlers.add(page1Handler)
        val page2Handler = Page2Handler(scannedDevices, recordingsManager)
        pageHandlers.add(page2Handler)
        val page3Handler = Page3Handler(scannedDevices)
        pageHandlers.add(page3Handler)
        page1Handler.addConnectionInterface(page2Handler)
        page1Handler.addConnectionInterface(page3Handler)
        pageHandlers.add(PermissionsHandler())
        for (handler in pageHandlers) {
            handler.activityCreated(this)
        }
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.colorPrimary)))

        ownCloudUploader = OwnCloudRecordingsUploader(this, recordingsManager)
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
            switcher.displayedChild = tab.position
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // TODO("Not yet implemented")
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // TODO("Not yet implemented")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    fun onFileUploadMenuItemClicked(ignored: MenuItem) {
        RecordingsUploaderDialog(this,  ownCloudUploader ).show()

    }

    fun onSettingsMenuItemClicked(ignored: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
