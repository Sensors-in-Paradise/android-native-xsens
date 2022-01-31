package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.xsens.dot.android.sdk.events.XsensDotData
import com.xsens.dot.android.sdk.models.XsensDotDevice
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.XSENSArrayList
import sensors_in_paradise.sonar.util.UIHelper

class Page2Handler(
    private val devices: XSENSArrayList,
    private val recordingsManager: RecordingDataManager
) : PageInterface, ConnectionInterface,
    TabLayout.OnTabSelectedListener {
    private lateinit var context: Context
    private lateinit var timer: Chronometer

    private lateinit var recyclerViewRecordings: RecyclerView
    private lateinit var activityCountTextView: TextView
    private lateinit var viewSwitcher: ViewSwitcher
    private lateinit var recordingsAdapter: RecordingsAdapter
    private lateinit var tabLayout: TabLayout
    private var activitiesTab: TabLayout.Tab? = null
    private var recordingsTab: TabLayout.Tab? = null
    private var numConnectedDevices = 0
    private var numDevices = 5

    private lateinit var loggingManager: LoggingManager
    private lateinit var activity: Activity
    override fun activityCreated(activity: Activity) {
        this.context = activity
        this.activity = activity
        timer = activity.findViewById(R.id.timer)

        activityCountTextView = activity.findViewById(R.id.tv_activity_counts)

        recyclerViewRecordings = activity.findViewById(R.id.recyclerView_recordings_captureFragment)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewRecordings.layoutManager = linearLayoutManager
        recordingsAdapter = RecordingsAdapter(recordingsManager, context)
        recyclerViewRecordings.adapter = recordingsAdapter
        viewSwitcher = activity.findViewById(R.id.viewSwitcher_captureFragment)
        tabLayout = activity.findViewById(R.id.tabLayout_captureFragment)

        activitiesTab = tabLayout.getTabAt(1)
        activitiesTab?.view?.isEnabled = false
        recordingsTab = tabLayout.getTabAt(0)
        loggingManager = LoggingManager(
            context,
            devices,
            activity.findViewById(R.id.buttonStart),
            activity.findViewById(R.id.buttonEnd),
            timer,
            activity.findViewById(R.id.tv_activity_captureFragment),
            activity.findViewById(R.id.tv_person_captureFragment),
            activity.findViewById(R.id.recyclerView_activities_captureFragment)
        )
        loggingManager.setOnRecordingDone { recording ->
            tabLayout.selectTab(recordingsTab)
            addRecordingToUI(
                recording
            )
            activitiesTab?.view?.isEnabled = false
        }
        loggingManager.setOnRecordingStarted {
            tabLayout.selectTab(activitiesTab)
            activitiesTab?.view?.isEnabled = true
        }

        tabLayout.addOnTabSelectedListener(this)
        updateActivityCounts()
    }

    private fun addRecordingToUI(recording: Recording) {
        recordingsManager.recordingsList.add(0, recording)
        recordingsAdapter.notifyItemInserted(0)
        updateActivityCounts()
    }

    private fun updateActivityCounts() {
        val numberOfRecodings = recordingsManager.getNumberOfRecordingsPerActivity()
        var text = " "
        for ((activity, number) in numberOfRecodings) {
            text += "$activity: $number | "
        }
        activityCountTextView.text = text.trimEnd('|', ' ')
    }

    override fun activityResumed() {
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        numConnectedDevices = devices.getConnected().size
        loggingManager.enoughDevicesConnected = numConnectedDevices >= numDevices
        val deviceLogger =
            loggingManager.xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }
        if (!connected && deviceLogger != null) {
            devices.get(deviceAddress)?.let {
                if (it.connectionState == XsensDotDevice.CONN_STATE_DISCONNECTED) {
                    loggingManager.cancelLogging()
                    UIHelper.showAlert(
                        context,
                        "The Device ${it.name} was disconnected!"
                    )
                    deviceLogger.stop()
                    it.stopMeasuring()
                    timer.stop()
                }
            }
        }
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        loggingManager.xsLoggers.find { logger -> logger.filename.contains(deviceAddress) }
            ?.update(xsensDotData)
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {}
    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab != null) {
            viewSwitcher.displayedChild = tab.position
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // TODO("Not yet implemented")
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // TODO("Not yet implemented")
    }
}
