package sensors_in_paradise.sonar.page2.camera.pose_estimation.data

data class PoseSequence(
    val timeStamps: ArrayList<Long>,
    val personsArray: ArrayList<List<Person>>,
    val startTime: Long
)
