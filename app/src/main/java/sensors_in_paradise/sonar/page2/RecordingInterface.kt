package sensors_in_paradise.sonar.page2

interface RecordingInterface {
    fun startRecording()
    fun stopRecording()
    fun startExporting()
    fun exportingFinished()
}
