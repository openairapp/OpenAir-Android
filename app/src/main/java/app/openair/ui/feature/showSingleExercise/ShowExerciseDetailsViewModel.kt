package app.openair.ui.feature.showSingleExercise

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import app.openair.model.AppRepository
import app.openair.model.database.ExerciseWithLocations
import app.openair.ui.formatter.FormattedExercise
import com.github.mikephil.charting.data.Entry
import com.google.android.gms.maps.model.LatLng

class ShowExerciseDetailsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val CHART_POINT_SERIES_ELEVATION = 0
        val CHART_POINT_SERIES_SPEED = 1
    }

    private var repository: AppRepository = AppRepository(application)
    private lateinit var exercise: LiveData<ExerciseWithLocations>
    lateinit var formattedExercise: LiveData<FormattedExercise>
    lateinit var mapPoints: LiveData<Array<LatLng>>

    /**
     * this live data contains a 2d array, with the first dimension being the chart,
     * and the second being all the point for the given chart
     * e.g. liveData`[chartIndex][pointIndex]`
     */
    lateinit var chartPoints: LiveData<Array<List<Entry>>>

    fun setExerciseId(exerciseId: Long) {
        exercise = repository.getExerciseWithLocations(exerciseId)

        formattedExercise = Transformations.map(exercise) { exerciseWithLocations ->
            FormattedExercise(exerciseWithLocations.exercise)
        }

        mapPoints = Transformations.map(exercise, ::getRouteMapPoints)
        chartPoints = Transformations.map(exercise, ::getChartPoints)
    }

    private fun getRouteMapPoints(data: ExerciseWithLocations): Array<LatLng> =
        data.locations
            .map { LatLng(it.latitude, it.longitude) }
            .toTypedArray()

    private fun getChartPoints(data: ExerciseWithLocations): Array<List<Entry>> {
        val startTime = data.exercise.startTime.time
        val elevationSeries = mutableListOf<Entry>()
        val speedSeries = mutableListOf<Entry>()

        data.locations.forEachIndexed { index, location ->
            // how long into the activity this point was recorded
            val relativeTime = (location.time - startTime).toFloat()

            // add our data to the series
            elevationSeries.add(index, Entry(relativeTime, location.elevation.toFloat()))
            speedSeries.add(index, Entry(relativeTime, location.speed))
        }

        // pack it all up and return it
        val result = mutableListOf<List<Entry>>()
        result.add(CHART_POINT_SERIES_ELEVATION, elevationSeries)
        result.add(CHART_POINT_SERIES_SPEED, speedSeries)
        return result.toTypedArray()
    }

    fun exportGPX(exerciseId: Long, destination: Uri) {
        repository.exportGPX(exerciseId, destination)
    }
}