package sensors_in_paradise.xsens

import android.app.Activity
import android.content.Context

interface PageInterface {
    fun activityCreated( activity: Activity)
    fun activityResumed( )
}