package sensors_in_paradise.sonar.screen_recording.camera.pose_estimation.data

data class PoseSequence(
    val timeStamps: ArrayList<Long>,
    val personsArray: ArrayList<List<Person>>,
    var startTime: Long
)
