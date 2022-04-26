package sensors_in_paradise.sonar

import android.app.Activity
import androidx.annotation.UiThread
import sensors_in_paradise.sonar.util.use_cases.UseCase

interface PageInterface {
    @UiThread
    fun activityCreated(activity: Activity)
    @UiThread
    fun activityResumed()
    @UiThread
    fun activityWillDestroy()
    @UiThread
    fun activityStopped() {}
    fun onUseCaseChanged(useCase: UseCase){}
}
