package sensors_in_paradise.sonar

import android.app.Activity

interface PageInterface {
    fun activityCreated(activity: Activity)
    fun activityResumed()
}
