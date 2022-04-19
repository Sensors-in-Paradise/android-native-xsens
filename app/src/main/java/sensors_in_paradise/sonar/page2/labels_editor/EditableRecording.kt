package sensors_in_paradise.sonar.page2.labels_editor

import sensors_in_paradise.sonar.page2.Recording
import sensors_in_paradise.sonar.page2.RecordingMetadataStorage

class EditableRecording(private val recording: Recording)
   {
       val metadataCopy = recording.metadataStorage.clone()
       val activities = metadataCopy.getActivities()
       fun getDurationOfActivity(index: Int): Long{
            val startTime = getRelativeStartTimeOfActivity(index)
            val endTime = getRelativeEndTimeOfActivity(index)
            return endTime - startTime
        }
       fun getActualTimeStarted():Long{
           return activities[0].timeStarted
       }
       fun getDuration():Long{
           return metadataCopy.getTimeEnded() - getActualTimeStarted()
       }
       fun getRelativeStartTimeOfActivity(index: Int):Long{
           return activities[index].timeStarted-getActualTimeStarted()
       }
       fun getRelativeEndTimeOfActivity(index: Int):Long{
           return (if(index+1<activities.size) activities[index+1].timeStarted else metadataCopy.getTimeEnded()) - getActualTimeStarted()
       }
}