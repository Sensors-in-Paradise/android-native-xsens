package sensors_in_paradise.sonar.page1

import java.util.HashMap

interface SyncInterface {
    fun finishedSyncOfDevice(address: String?, isSuccess: Boolean)
    fun finishedSynching(syncingResultMap: HashMap<String, Boolean>?, isSuccess: Boolean, requestCode: Int)
}