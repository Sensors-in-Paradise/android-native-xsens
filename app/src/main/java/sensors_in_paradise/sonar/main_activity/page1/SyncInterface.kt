package sensors_in_paradise.sonar.main_activity.page1

import androidx.annotation.AnyThread
import java.util.HashMap

interface SyncInterface {
    @AnyThread
    fun onFinishedSyncOfDevice(address: String?, isSuccess: Boolean)
    @AnyThread
    fun onFinishedSyncing(syncingResultMap: HashMap<String, Boolean>?, isSuccess: Boolean, requestCode: Int)
    @AnyThread
    fun onSyncProgress(progress: Int)
}
