@file:Suppress("SwallowedException")
package sensors_in_paradise.sonar.screen_recording

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sensors_in_paradise.sonar.JSONStorage
import java.io.File

class RecordingMetadataStorage(file: File) : JSONStorage(file) {
    private lateinit var activities: JSONArray
    override fun onFileNewlyCreated() {
        activities = JSONArray()
        json.put("activities", activities)
        save()
    }

    override fun onJSONInitialized() {
        activities = json.getJSONArray("activities")
    }

    fun setData(
        activities: ArrayList<Pair<Long, String>>,
        totalStartTime: Long,
        endTime: Long,
        person: String,
        sensorMacMap: Map<String, String>
    ) {
        for (activity in activities) {
            addActivity(activity)
        }
        setTimeFinished(endTime)
        setTimeStarted(totalStartTime)
        setPerson(person)
        setSensorMacMap(sensorMacMap)
        save()
    }
    fun setVideoCaptureStartedTime(timeStarted: Long, save: Boolean = false) {
        json.put("videoCaptureStartTime", timeStarted)
        if (save) {
            save()
        }
    }
    fun getActivities(): ArrayList<Pair<Long, String>> {
        val result = ArrayList<Pair<Long, String>>()
        for (i in 0 until activities.length()) {
            val activityObj = activities[i] as JSONObject
            val timeStarted = activityObj.getLong("timeStarted")
            val label = activityObj.getString("label")
            result.add(Pair(timeStarted, label))
        }
        return result
    }
    fun getVideoCaptureStartedTime(): Long? {
        val v = json.optLong("videoCaptureStartTime")
        return if (v != 0L) v else null
    }
    fun getDuration(): Long {
        return getTimeEnded() - getTimeStarted()
    }
    fun getTimeStarted(): Long {
        return json.getLong("startTimestamp")
    }
    fun getTimeEnded(): Long {
        return json.getLong("endTimestamp")
    }
    fun getPerson(): String {
        return json.getString("person")
    }

    fun setRecordingState(state: String) {
        json.put("recordingState", state)
        save()
    }

    fun getRecordingState(): String? {
        return try {
            json.getString("recordingState")
        } catch (exception: JSONException) {
            null
        }
    }

    private fun addActivity(activity: Pair<Long, String>) {
        val obj = JSONObject()
        obj.put("timeStarted", activity.first)
        obj.put("label", activity.second)
        activities.put(obj)
    }

    private fun setTimeStarted(startTime: Long) {
        json.put("startTimestamp", startTime)
    }

    private fun setTimeFinished(endTime: Long) {
        json.put("endTimestamp", endTime)
    }

    private fun setPerson(person: String) {
        json.put("person", person)
    }
    private fun setSensorMacMap(sensorMacMap: Map<String, String>) {
        val obj = JSONObject()
        for (entry in sensorMacMap.entries) {
            obj.put(entry.key, entry.value)
        }
        json.put("sensorMapping", obj)
    }
}
