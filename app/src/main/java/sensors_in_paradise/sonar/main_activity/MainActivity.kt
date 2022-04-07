package sensors_in_paradise.sonar.main_activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ViewAnimator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.SettingsActivity
import sensors_in_paradise.sonar.main_activity.all_pages.GlobalValues
import sensors_in_paradise.sonar.main_activity.all_pages.PageInterface
import sensors_in_paradise.sonar.main_activity.all_pages.PermissionsHandler
import sensors_in_paradise.sonar.main_activity.all_pages.SensorTrafficVisualizationHandler
import sensors_in_paradise.sonar.main_activity.all_pages.xsens.XSENSArrayList
import sensors_in_paradise.sonar.main_activity.custom_views.stickman.StickmanDialog
import sensors_in_paradise.sonar.main_activity.page1.Page1Handler
import sensors_in_paradise.sonar.main_activity.page2.Page2Handler
import sensors_in_paradise.sonar.main_activity.page2.recordings.RecordingDataManager
import sensors_in_paradise.sonar.main_activity.page3.Page3Handler
import sensors_in_paradise.sonar.main_activity.uploader.RecordingsUploaderDialog
import sensors_in_paradise.sonar.main_activity.uploader.DavCloudRecordingsUploader
import sensors_in_paradise.sonar.main_activity.util.PreferencesHelper

class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {

    private lateinit var switcher: ViewAnimator
    private lateinit var tabLayout: TabLayout
    private lateinit var davCloudUploader: DavCloudRecordingsUploader
    private lateinit var recordingsManager: RecordingDataManager

    private val pageHandlers = ArrayList<PageInterface>()
    private val scannedDevices = XSENSArrayList()
    private lateinit var page1Handler: Page1Handler
    private lateinit var sensorTrafficVisualizationHandler: SensorTrafficVisualizationHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switcher = findViewById(R.id.switcher_activity_main)
        tabLayout = findViewById(R.id.tab_layout_activity_main)
        recordingsManager = RecordingDataManager(
            GlobalValues.getSensorRecordingsBaseDir(this)
        )

        initClickListeners()
        page1Handler = Page1Handler(scannedDevices)
        pageHandlers.add(page1Handler)
        val page2Handler = Page2Handler(scannedDevices, recordingsManager)
        pageHandlers.add(page2Handler)
        val page3Handler = Page3Handler(scannedDevices)
        pageHandlers.add(page3Handler)
        page1Handler.addConnectionInterface(page2Handler)
        page1Handler.addConnectionInterface(page3Handler)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {}
        pageHandlers.add(PermissionsHandler(permissionLauncher))

        for (handler in pageHandlers) {
            handler.activityCreated(this)
        }
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.colorPrimary)))

        davCloudUploader = DavCloudRecordingsUploader(this, recordingsManager)

        // Force crashlytics to be enabled (we might want to disable it in debug mode / ...)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        var mode = MODE_NIGHT_FOLLOW_SYSTEM
        if (!PreferencesHelper.shouldFollowSystemTheme(this)) {
            mode = if (PreferencesHelper.shouldUseDarkMode(this)) MODE_NIGHT_YES else MODE_NIGHT_NO
        }
        setDefaultNightMode(mode)
        for (handler in pageHandlers) {
            handler.activityResumed()
        }
    }

    override fun onDestroy() {
        pageHandlers.forEach { handler -> handler.activityWillDestroy() }

        super.onDestroy()
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
        sensorTrafficVisualizationHandler = SensorTrafficVisualizationHandler(
            this,
            scannedDevices,
            findViewById(R.id.sensorDataTrafficIndicator_captureFragment),
            findViewById(R.id.linearLayout_sensorOrientation_activityMain),
            menu.findItem(R.id.menuItem_orientation_activityMain)
        )
        page1Handler.addConnectionInterface(
            sensorTrafficVisualizationHandler
        )
        return true
    }

    fun onFileUploadMenuItemClicked(ignored: MenuItem) {
        RecordingsUploaderDialog(this, davCloudUploader).show()
    }

    fun onSettingsMenuItemClicked(ignored: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    fun onStickmanMenuItemClicked(ignored: MenuItem) {
        StickmanDialog(this)
    }
    fun onOrientationMenuItemClicked(mI: MenuItem) {
        mI.isChecked = !mI.isChecked
        sensorTrafficVisualizationHandler.setOrientationVisible(mI.isChecked)
    }
}
