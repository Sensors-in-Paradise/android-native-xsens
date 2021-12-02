package sensors_in_paradise.sonar.file_uploader

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.Button
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import sensors_in_paradise.sonar.GlobalValues
import sensors_in_paradise.sonar.R
import java.io.File
import java.lang.Exception


class FileUploaderDialog(activity: Activity): AlertDialog(activity), FileUploaderInterface{
    val context = activity
    private val uploader = FileUploader(GlobalValues.getSensorDataBaseDir(context), this)
    private val fileItems = FileUIItemArrayList()
    private val adapter = FilesAdapter(fileItems)
    private var hintTV: TextView
    private var noFilesTV: TextView
    private var switcher:ViewSwitcher
    private var uploadButton:Button
    init{

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
        val filesToBeUploaded =  uploader.getFilesToBeUploaded(GlobalValues.getSensorDataBaseDir(context))
        for(file in filesToBeUploaded){
            val suffix = uploader.getURLSuffixForFile(file)
            fileItems.add(FileUIItem(file, suffix))
        }
        if(filesToBeUploaded.size>0){
            noFilesTV.visibility = View.GONE
            uploadButton.visibility = View.VISIBLE
        }
        uploadButton.setOnClickListener {
            setCancelable(false)
            uploadButton.isEnabled  = false
            uploadButton.text = "Uploading"
            uploader.uploadFiles(context)
        }
        retryButton.setOnClickListener {
            switcher.displayedChild = 0
            fileItems.setStatusOfAllItems(UploadStatus.NOT_UPLOADED)
            uploadButton.isEnabled  = false
            uploadButton.text = "Uploading"
            uploader.uploadFiles(context)
        }
    }

    override fun onConnectionFailed(hint: String) {
        switcher.displayedChild = 1
    }

    override fun onFileUploaded(file: File) {
        updateStatus(file, UploadStatus.UPLOADED, null)
    }

    override fun onFileUploadFailed(file: File, exception: Exception) {
        updateStatus(file, UploadStatus.FAILED, exception)
    }

    override fun onFileUploadStarted(file: File) {
        updateStatus(file, UploadStatus.UPLOADING, null)
    }
    private fun updateStatus(file: File, status: UploadStatus, error: Exception?){
        val item = fileItems.get(file)
        item?.status = status
        item?.error = error
        adapter.notifyItemChanged(file)
        if(fileItems.areAllUploadedOrFailed()){
            uploadButton.isEnabled  = true
            uploadButton.text = "Upload"
            setCancelable(true)
            if(fileItems.areAllUploaded()){
                uploadButton.text = "All uploaded"
                uploadButton.isEnabled  = false
            }
        }

    }
}