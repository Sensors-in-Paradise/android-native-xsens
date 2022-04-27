package sensors_in_paradise.sonar.screen_train

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import sensors_in_paradise.sonar.JSONStorage
import sensors_in_paradise.sonar.util.use_cases.UseCase
import java.io.File

class TrainingHistoryStorage(useCase: UseCase) :
    JSONStorage(useCase.getTrainingHistoryJSONFile()) {
    data class TrainingOccasion(
        val timestamp: Long,
        val peopleDurations: Map<String, Long>,
        val activityDurations: Map<String, Long>
    ) {
        fun getTotalDuration(): Long {
            var sum = 0L
            for ((_, duration) in peopleDurations) {
                sum += duration
            }
            return sum
        }

        fun getNumPeople(): Int {
            return peopleDurations.keys.size
        }

        fun getNumActivities(): Int {
            return activityDurations.keys.size
        }
    }

    private lateinit var history: JSONArray
    override fun onFileNewlyCreated() {
        json.put("history", JSONArray())
    }

    override fun onJSONInitialized() {
        history = json.getJSONArray("history")
    }

    fun addTrainingOccasion(
        trainingDataPeopleDistribution: Map<String, Long>,
        trainingDataActivityDistribution: Map<String, Long>
    ): TrainingOccasion {
        val occasionObj = JSONObject()
        val peopleDurationObj = storeMapInJsonObj(trainingDataPeopleDistribution)
        val activityDurationObj = storeMapInJsonObj(trainingDataActivityDistribution)
        occasionObj.put("peopleDurations", peopleDurationObj)
        occasionObj.put("activityDurations", activityDurationObj)
        val time = System.currentTimeMillis()
        occasionObj.put("timestamp", time)
        history.put(occasionObj)
        save()
        return TrainingOccasion(
            time,
            trainingDataPeopleDistribution,
            trainingDataActivityDistribution
        )
    }

    fun getTrainingHistory(sortLatestFirst: Boolean = true): ArrayList<TrainingOccasion> {
        val result = arrayListOf<TrainingOccasion>()
        for (i in 0 until history.length()) {
            val obj = history.getJSONObject(i)
            val timestamp = obj.getLong("timestamp")
            val activityDurations = getMapFromJsonObj(obj.getJSONObject("activityDurations"))
            val peopleDurations = getMapFromJsonObj(obj.getJSONObject("peopleDurations"))
            result.add(TrainingOccasion(timestamp, peopleDurations, activityDurations))
        }
        if (sortLatestFirst) {
            result.sortByDescending { it.timestamp }
        }
        return result
    }

    private fun storeMapInJsonObj(map: Map<String, Long>): JSONObject {
        val result = JSONObject()
        for ((key, value) in map) {
            result.put(key, value)
        }
        return result
    }

    private fun getMapFromJsonObj(obj: JSONObject): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        val keys = obj.keys()
        for (key in keys) {
            result[key] = obj.getLong(key)
        }
        return result
    }

}
