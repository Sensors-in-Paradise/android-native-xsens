package sensors_in_paradise.sonar.main_activity.all_pages

import android.app.Activity
import androidx.annotation.UiThread

interface PageInterface {
    @UiThread
    fun activityCreated(activity: Activity)
    @UiThread
    fun activityResumed()
    @UiThread
    fun activityWillDestroy()
    @UiThread
    fun activityStopped() {}
}
