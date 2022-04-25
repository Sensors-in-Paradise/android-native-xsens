package sensors_in_paradise.sonar

import android.app.Activity
import androidx.annotation.UiThread

interface ScreenInterface {
    @UiThread
    fun onActivityCreated(activity: Activity)
    @UiThread
    fun onActivityResumed() {}
    @UiThread
    fun onActivityWillDestroy() {}
    @UiThread
    fun onActivityStopped() {}
    @UiThread
    fun onScreenOpened() {}
    @UiThread
    fun onScreenClosed() {}
}
