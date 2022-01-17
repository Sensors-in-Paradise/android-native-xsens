package sensors_in_paradise.sonar.uploader

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import sensors_in_paradise.sonar.page2.RecordingDataManager
import java.io.File
import java.lang.Exception

class RecordingsUploaderDialog(activity: Activity, uploader: OwnCloudRecordingsUploader) : AlertDialog(activity) {
    val context = activity
    private val uploader = OwnCloudRecordingsUploader(activity, uploader.recordingsManager)
    private val recordings = uploader.recordingUiItems
    private val adapter = RecordingsUploadAdapter(recordings)
    private var hintTV: TextView
    private var noFilesTV: TextView
    private var switcher: ViewSwitcher
    private var uploadButton: Button
    init {

        val inflater = activity.layoutInflater
        val rootView = inflater.inflate(R.layout.upload_dialog, null)
        setView(rootView)
        uploadButton = rootView.findViewById(R.id.button_upload_uploadDialog)
        val retryButton = rootView.findViewById<Button>(R.id.button_retry_uploadFiles)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.rv_files_uploadDialog)
        switcher = rootView.findViewById(R.id.switcher_connectionStatus_uploadDialog)
        hintTV = rootView.findViewById(R.id.tv_hint_uploadFilesDialog)
        noFilesTV = rootView.findViewById(R.id.tv_noFilesToUpload_uploadDialog)
        recyclerView.adapter = adapter

        if (recordings.size> 0) {
            noFilesTV.visibility = View.GONE
            uploadButton.visibility = View.VISIBLE
        }
        uploadButton.setOnClickListener {
            setCancelable(false)
            uploadButton.isEnabled = false
            uploadButton.text = "Uploading"
            uploader.synchronize()
        }
        retryButton.setOnClickListener {
            switcher.displayedChild = 0

            uploadButton.isEnabled = false
            uploadButton.text = "Uploading"
            uploader.synchronize()
        }
        uploader.onItemChanged =  this::onRecordingItemChanged
    }

    private fun onRecordingItemChanged(recording: RecordingUIItem){
        adapter.notifyItemChanged(recordings.indexOf(recording))
    }
}