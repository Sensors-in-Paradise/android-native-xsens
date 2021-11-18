package sensors_in_paradise.sonar.page1

import com.xsens.dot.android.sdk.interfaces.XsensDotSyncCallback
import com.xsens.dot.android.sdk.models.XsensDotSyncManager
import java.util.HashMap

class SyncHandler(val callback: SyncInterface): XsensDotSyncCallback {
    override fun onSyncingStarted(address: String?, isSuccess: Boolean, requestCode: Int) {

    }

    override fun onSyncingProgress(progress: Int, requestCode: Int) {

    }

    override fun onSyncingResult(address: String?, isSuccess: Boolean, requestCode: Int) {
        callback.finishedSyncOfDevice(address, isSuccess)
    }

    override fun onSyncingDone(syncingResultMap: HashMap<String, Boolean>?, isSuccess: Boolean, requestCode: Int) {

    }

    override fun onSyncingStopped(address: String?, isSuccess: Boolean, requestCode: Int) {

    }

}