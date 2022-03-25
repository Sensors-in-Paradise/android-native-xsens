package sensors_in_paradise.sonar

import android.app.Activity
import androidx.annotation.UiThread

interface PageInterface {
    @UiThread
    fun activityCreated(activity: Activity)
    @UiThread
    fun activityResumed()
    @UiThread
    fun activityWillDestroy()
}
