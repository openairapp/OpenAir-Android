package app.openair.model.logic

import android.content.Context
import androidx.work.*
import app.openair.model.AppRepository
import app.openair.model.database.Exercise
import app.openair.model.database.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import android.location.Location as AndroidLocation

class ExerciseWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val EXERCISE_ID_KEY = "exerciseId"

        fun schedule(context: Context, exerciseId: Long, tag: String) {
            Timber.d("scheduling processing for exercise data")
            val workRequest = OneTimeWorkRequest.Builder(ExerciseWorker::class.java)

            // pass in the exercise ID
            val data = Data.Builder()
            data.putLong(EXERCISE_ID_KEY, exerciseId)
            workRequest.setInputData(data.build())

            // add a tag so we can check on it later in the main activity
            workRequest.addTag(tag)

            WorkManager.getInstance(context).enqueue(workRequest.build())
        }
    }

    override fun doWork(): Result {
        val repository = AppRepository(context)
        val exerciseId = inputData.getLong(EXERCISE_ID_KEY, 0)
        val exerciseData = repository.getExerciseWithLocationsStatic(exerciseId)

        Timber.d("processing exercise: id=$exerciseId, name=${exerciseData.exercise.name}, locations=${exerciseData.locations.size}")

        val duration = calculateDuration(exerciseData.exercise)
        if (duration == 0L) {
            Timber.w("unable to process exercise with duration of 0")
            return Result.failure()
        }

        val distance = calculateTotalDistance(exerciseData.locations)
        val averageSpeed = calculateAverageSpeed(distance, duration)
        val elevationGain = calculateTotalClimb(exerciseData.locations)

        CoroutineScope(Dispatchers.IO).launch {
            repository.addCalculatedValues(
                exerciseId,
                duration,
                distance,
                averageSpeed,
                elevationGain
            )
        }

        return Result.success()
    }

    /**
     * calculate the total length of the exercise in milliseconds
     */
    private fun calculateDuration(exercise: Exercise): Long =
        (exercise.endTime.time - exercise.startTime.time)

    /**
     * calculate the average speed over the exercise in meters per second
     */
    private fun calculateAverageSpeed(distance: Float, duration: Long) =
        distance / ((duration).toFloat() / 1000)

    /**
     * calculate the total distance traveled for the exercise in meters
     */
    private fun calculateTotalDistance(locations: List<Location>): Float =
        calculateDistanceDifferentials(locations)
            .sum()

    /**
     * create a List containing the distances between each location in meters
     */
    private fun calculateDistanceDifferentials(locations: List<Location>): List<Float> =
        calculateDifferential(locations, fun(prev: Location, curr: Location): Float {

            val resultArray = FloatArray(1)

            AndroidLocation.distanceBetween(
                prev.latitude,
                prev.longitude,
                curr.latitude,
                curr.longitude,
                resultArray
            )
            return resultArray[0]
        })


    /**
     * calculate the total altitude gained in meters
     */
    private fun calculateTotalClimb(locations: List<Location>): Float =
        calculateElevationDifferentials(locations)
            .filter { it > 0 }
            .sum()


    /**
     * create a List containing the elevation difference between each recorded location in meters
     */
    private fun calculateElevationDifferentials(locations: List<Location>): List<Float> =
        calculateDifferential(locations) { prev: Location, curr: Location ->
            (curr.elevation - prev.elevation).toFloat()
        }

    /**
     * For a list of any type, calculate the difference between each adjacent element.
     *
     * Must be provided with a function that can determine the difference between two given points
     * and return a [Float]
     *
     * The resultant list will be one element shorter since the elements can't be compared to nothing
     */
    private fun <T> calculateDifferential(
        items: List<T>,
        differentiator: (previousElement: T, currentElement: T) -> Float
    ): List<Float> =
        items.toMutableList()
            .drop(1)
            .mapIndexed { index, it -> differentiator(items[index], it) }
            .toList()
}