package sensors_in_paradise.sonar

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import android.widget.ViewAnimator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.owncloud.android.lib.common.OwnCloudClientFactory
import com.owncloud.android.lib.common.authentication.OwnCloudCredentialsFactory
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import sensors_in_paradise.sonar.uploader.FileUploaderDialog
import sensors_in_paradise.sonar.page1.Page1Handler
import sensors_in_paradise.sonar.page2.Page2Handler
import sensors_in_paradise.sonar.page3.Page3Handler
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation
import com.owncloud.android.lib.resources.files.FileUtils


class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener, OnRemoteOperationListener {

    private lateinit var switcher: ViewAnimator
    private lateinit var tabLayout: TabLayout

    private val pageHandlers = ArrayList<PageInterface>()

    private val scannedDevices = XSENSArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switcher = findViewById(R.id.switcher_activity_main)
        tabLayout = findViewById(R.id.tab_layout_activity_main)

        initClickListeners()
        val page1Handler = Page1Handler(scannedDevices)
        pageHandlers.add(page1Handler)
        val page2Handler = Page2Handler(scannedDevices)
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

        val serverUri: Uri = Uri.parse("https://owncloud.hpi.de/")
        val client = OwnCloudClientFactory.createOwnCloudClient(
            serverUri,
            this,
            // Activity or Service context
            true);
        client.credentials = OwnCloudCredentialsFactory.newBasicCredentials("tobias.fiedler", "EniXSSatM8")
        val createOperation = CreateRemoteFolderOperation("/Hallo", false)
        createOperation.execute(client, this, null)
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
        FileUploaderDialog(this).show()
    }

    fun onSettingsMenuItemClicked(ignored: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onRemoteOperationFinish(
        operation: RemoteOperation<*>?,
        result: RemoteOperationResult<*>?
    ) {
        if (operation is CreateRemoteFolderOperation) {
            if (result != null) {
                val success = result.isSuccess
                Toast.makeText(this, "folder creation successful: $success", Toast.LENGTH_LONG).show()

            }
        }
    }
}
