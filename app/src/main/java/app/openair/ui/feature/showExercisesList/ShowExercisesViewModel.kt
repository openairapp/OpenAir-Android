package app.openair.ui.feature.showExercisesList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import app.openair.model.AppRepository
import app.openair.model.database.Exercise
import app.openair.ui.formatter.FormatUtil
import app.openair.ui.formatter.FormattedExercise
import kotlinx.coroutines.launch

class ShowExercisesViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: AppRepository = AppRepository(application)
    var exercises: LiveData<List<FormattedExercise>>
    var processingExercise: LiveData<Boolean>

    init {
        // convert the exercise object into strings for the ui to show
        exercises = Transformations.map(
            repository.getAllExercises(),
            ::formatExercises
        )

        // see if there are any processing tasks that are running or due to run
        processingExercise = repository.getRunningProcessingJobs()
    }

    private fun formatExercises(exercises: List<Exercise>): List<FormattedExercise> =
        exercises.map { FormattedExercise(it) }

    fun formatTime(time: Long): String = FormatUtil.formatSecondsToString(time)

    fun formatDistance(distance: Float): String = FormatUtil.formatMetersToKilometersString(distance)

    fun deleteExercises(ids: List<Long>){
        viewModelScope.launch {
            repository.deleteExercises(ids)
        }
    }
}