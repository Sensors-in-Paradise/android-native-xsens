package sensors_in_paradise.sonar.page2

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.*
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.common.io.Files
import com.xsens.dot.android.sdk.events.XsensDotData
import sensors_in_paradise.sonar.*
import sensors_in_paradise.sonar.page1.ConnectionInterface
import sensors_in_paradise.sonar.XSENSArrayList
import sensors_in_paradise.sonar.util.PreferencesHelper
import java.io.IOException

class Page2Handler(
    private val devices: XSENSArrayList,
    private val recordingsManager: RecordingDataManager,
    private val sensorOccupationInterface: SensorOccupationInterface?
) : PageInterface, ConnectionInterface,
    TabLayout.OnTabSelectedListener {
    private lateinit var context: Context
    private lateinit var timer: Chronometer

    private lateinit var recyclerViewRecordings: RecyclerView
    private lateinit var viewAnimator: ViewAnimator
    private lateinit var recordingsAdapter: RecordingsAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var activitiesCenterTV: TextView
    private var activitiesTab: TabLayout.Tab? = null
    private var recordingsTab: TabLayout.Tab? = null
    private lateinit var cameraTab: TabLayout.Tab
    private var numConnectedDevices = 0

    private lateinit var loggingManager: LoggingManager
    private lateinit var activity: Activity
    private lateinit var cameraManager: CameraManager

    override fun activityCreated(activity: Activity) {
        this.context = activity
        this.activity = activity
        timer = activity.findViewById(R.id.timer)

        recyclerViewRecordings = activity.findViewById(R.id.recyclerView_recordings_captureFragment)
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerViewRecordings.layoutManager = linearLayoutManager
        recordingsAdapter = RecordingsAdapter(recordingsManager, context)
        recyclerViewRecordings.adapter = recordingsAdapter
        viewAnimator = activity.findViewById(R.id.viewSwitcher_captureFragment)
        tabLayout = activity.findViewById(R.id.tabLayout_captureFragment)
        activitiesCenterTV = activity.findViewById(R.id.textView_no_activities_captureFragment)
        activitiesTab = tabLayout.getTabAt(1)
        recordingsTab = tabLayout.getTabAt(0)

        cameraTab = tabLayout.newTab().apply {
            text = "Camera"
        }

        loggingManager = LoggingManager(
            context,
            devices,
            activity.findViewById(R.id.buttonRecord),
            timer,
            activity.findViewById(R.id.tv_activity_captureFragment),
            activity.findViewById(R.id.tv_person_captureFragment),
            activity.findViewById(R.id.recyclerView_activities_captureFragment)
        )
        initializeLoggingManagerCallbacks()

        tabLayout.addOnTabSelectedListener(this)
        cameraManager =
            CameraManager(context, activity.findViewById(R.id.previewView_camera_captureFragment))
    }

    private fun initializeLoggingManagerCallbacks() {
        loggingManager.setOnRecordingDone { recording ->
            tabLayout.selectTab(recordingsTab)
            addRecordingToUI(
                recording
            )
            activitiesCenterTV.visibility = View.VISIBLE
        }
        loggingManager.setOnRecordingStarted {
            sensorOccupationInterface?.onSensorOccupationStatusChanged(true)
            if (tabLayout.selectedTabPosition != 2) {
                tabLayout.selectTab(activitiesTab)
            }
            activitiesCenterTV.visibility = View.GONE
            if (cameraManager.shouldRecordVideo()) {
                val dir = GlobalValues.getVideoRecordingsTempDir(context)
                dir.mkdir()
                cameraManager.startRecording(
                    dir.resolve(
                        "before_" + System.currentTimeMillis().toString() + ".mp4"
                    )
                )
            }
        }
        loggingManager.setOnFinalizingRecording { dir, metadata ->
            cameraManager.stopRecording { videoCaptureStartTime, videoTempFile ->
                metadata.setVideoCaptureStartedTime(videoCaptureStartTime, true)
                try {
                    Files.move(videoTempFile, dir.resolve(Recording.VIDEO_CAPTURE_FILENAME))
                    val recordingIndex =
                        recordingsManager.recordingsList.indexOfFirst { r -> r.dir == dir }
                    recordingsAdapter.notifyItemChanged(recordingIndex)
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }
            sensorOccupationInterface?.onSensorOccupationStatusChanged(false)
        }
    }

    private fun addRecordingToUI(recording: Recording) {
        recordingsManager.recordingsList.add(0, recording)
        recordingsAdapter.notifyItemInserted(0)
    }

    override fun activityResumed() {
        setCameraTabVisible(PreferencesHelper.shouldRecordWithCamera(context))
    }

    override fun activityWillDestroy() {
        loggingManager.stopLoggingImmediately()
    }

    override fun onConnectedDevicesChanged(deviceAddress: String, connected: Boolean) {
        numConnectedDevices = devices.getConnected().size
        loggingManager.handleConnectionStateChange(deviceAddress, connected)
    }

    override fun onXsensDotDataChanged(deviceAddress: String, xsensDotData: XsensDotData) {
        loggingManager.handleSensorDataUpdate(deviceAddress, xsensDotData)
    }

    override fun onXsensDotOutputRateUpdate(deviceAddress: String, outputRate: Int) {}

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab != null) {
            viewAnimator.displayedChild = tab.position
            if (tab == cameraTab) {
                cameraManager.bindPreview()
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        if (tab == cameraTab) {
            cameraManager.unbindPreview()
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // TODO("Not yet implemented")
    }

    private fun setCameraTabVisible(visible: Boolean) {
        if (visible) {
            if (tabLayout.tabCount < 3) {
                tabLayout.addTab(cameraTab)
            }
        } else {
            if (tabLayout.tabCount > 2) {
                tabLayout.removeTab(cameraTab)
            }
        }
    }
}
