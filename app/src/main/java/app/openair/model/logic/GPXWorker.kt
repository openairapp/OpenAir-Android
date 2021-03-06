package app.openair.model.logic

import android.content.Context
import android.net.Uri
import androidx.work.*
import app.openair.model.AppRepository
import io.jenetics.jpx.GPX
import io.jenetics.jpx.Track
import io.jenetics.jpx.TrackSegment
import io.jenetics.jpx.WayPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class GPXWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val EXERCISE_ID_KEY = "exerciseId"
        const val SAVE_DIRECTORY_URI = "saveUri"

        fun schedule(context: Context, exerciseId: Long, fileUri: Uri) {
            Timber.d("scheduling export for exercise data")
            val workRequest = OneTimeWorkRequest.Builder(GPXWorker::class.java)

            // pass in the exercise ID
            val data = Data.Builder()
            data.putLong(EXERCISE_ID_KEY, exerciseId)
            data.putString(SAVE_DIRECTORY_URI, fileUri.toString())
            workRequest.setInputData(data.build())

            WorkManager.getInstance(context).enqueue(workRequest.build())
        }
    }

    override fun doWork(): Result {
        val repository = AppRepository(context)
        val exerciseId = inputData.getLong(EXERCISE_ID_KEY, 0)
        val fileUri = Uri.parse(inputData.getString(SAVE_DIRECTORY_URI))
        val exerciseData = repository.getExerciseWithLocationsStatic(exerciseId)

        Timber.d("exporting exercise: id=$exerciseId, name=${exerciseData.exercise.name}, locations=${exerciseData.locations.size}")

        if (exerciseData.locations.isEmpty()) {
            Timber.w("unable to export exercise with 0 location markers")
            return Result.failure()
        }

        // put all the location data for the provided exercise into a gpx file
        val gpx = GPX.builder()
            .addTrack { track: Track.Builder ->
                track.addSegment { segment: TrackSegment.Builder ->

                    exerciseData.locations.forEach { location ->
                        segment.addPoint { p: WayPoint.Builder ->

                            p.apply {
                                lat(location.latitude)
                                lon(location.longitude)
                                ele(location.elevation)
                                time(location.time)
                            }
                        }
                    }
                }
            }
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            @Suppress("BlockingMethodInNonBlockingContext") // we are using the IO coroutine context so it is ok to block
            val outputStream = context.contentResolver.openOutputStream(fileUri)

            @Suppress("BlockingMethodInNonBlockingContext")
            GPX.write(gpx, outputStream)

            @Suppress("BlockingMethodInNonBlockingContext")
            outputStream?.close()
        }

        return Result.success()

    }
}