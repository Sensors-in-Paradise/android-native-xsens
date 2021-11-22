package sensors_in_paradise.sonar.page1

import java.util.HashMap

interface SyncInterface {
    fun onFinishedSyncOfDevice(address: String?, isSuccess: Boolean)
    fun onFinishedSyncing(syncingResultMap: HashMap<String, Boolean>?, isSuccess: Boolean, requestCode: Int)
    fun onSyncProgress(progress: Int)
}
