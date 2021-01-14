package app.openair.model

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import app.openair.model.database.*
import app.openair.model.logic.DateUtil
import app.openair.model.logic.ExerciseWorker
import app.openair.model.logic.GPXWorker
import app.openair.model.logic.TimePeriodStats
import app.openair.types.StatsPeriod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

class AppRepository(val context: Context) {

    private var database: AppDatabase = AppDatabase.getDatabase(context)!!

    private var exerciseDao: ExerciseDao
    private var locationDao: LocationDao

    companion object {
        const val PROCESS_EXERCISE_WORK_TAG = "processExercise"
    }

    init {
        exerciseDao = database.exerciseDao()
        locationDao = database.locationDao()
    }

    fun getAllExercises(): LiveData<List<Exercise>> {
        return exerciseDao.getAll().asLiveData()
    }

    fun getExercise(exerciseId: Long): LiveData<Exercise> {
        return exerciseDao.get(exerciseId).asLiveData()
    }

    fun getExerciseWithLocations(exerciseId: Long): LiveData<ExerciseWithLocations> {
        return exerciseDao.getWithLocations(exerciseId).asLiveData()
    }

    fun getExerciseWithLocationsStatic(exerciseId: Long): ExerciseWithLocations {
        return exerciseDao.getStatic(exerciseId)
    }

    suspend fun addExercise(exercise: Exercise): Long {
        return exerciseDao.insert(exercise)
    }

    suspend fun logLocation(location: List<Location>) {
        locationDao.insert(location)
    }

    suspend fun addFinishData(exerciseId: Long, name: String, notes: String, endTime: Date) {
        exerciseDao.addFinishData(exerciseId, name, notes, endTime)

        // process the data we've just added
        ExerciseWorker.schedule(context, exerciseId, PROCESS_EXERCISE_WORK_TAG)
    }

    fun getRunningProcessingJobs(): LiveData<Boolean> {
        return Transformations.map(
            // Get a list of work manager jobs for the tag we specified
            WorkManager.getInstance(context.applicationContext)
                .getWorkInfosByTagLiveData(PROCESS_EXERCISE_WORK_TAG)
        ) { workInfoList ->
            // go through the list, and find if any jobs are still running or waiting to be run
            workInfoList
                .map { it.state }
                .any { it == WorkInfo.State.ENQUEUED || it == WorkInfo.State.RUNNING }
        }
    }

    fun exportGPX(exerciseId: Long, destination: Uri) {
        GPXWorker.schedule(context, exerciseId, destination)
    }

    suspend fun updateExercise(exerciseId: Long, name: String, notes: String) {
        exerciseDao.update(exerciseId, name, notes)
    }

    suspend fun addCalculatedValues(
        exerciseId: Long,
        duration: Long,
        distance: Float,
        averageSpeed: Float,
        elevationGain: Float
    ) {
        exerciseDao.addCalculatedValues(
            exerciseId,
            duration,
            distance,
            averageSpeed,
            elevationGain
        )
    }

    suspend fun deleteExercise(exerciseId: Long) {
        exerciseDao.delete(exerciseId)
    }

    suspend fun deleteExercises(exerciseId: List<Long>) {
        exerciseDao.delete(exerciseId)
    }

    @ExperimentalCoroutinesApi
    suspend fun getTimeSeriesStats(period: StatsPeriod): LiveData<TimePeriodStats>{
        val endDate = Date()
        val startDate = DateUtil.getDateAt(endDate, period)
        val exercise = exerciseDao.getBetween(startDate, endDate).asLiveData()

        return Transformations.map(exercise) {
            val stats = TimePeriodStats(period)
            stats.setData(it, startDate, endDate)

            return@map stats
        }
    }
}
