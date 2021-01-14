package app.openair.ui.feature.recordExercise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.openair.R
import app.openair.model.AppRepository
import app.openair.model.database.Exercise
import kotlinx.coroutines.launch

class SetupExerciseViewModel(private val parentApplication: Application): AndroidViewModel(parentApplication) {
    private var repository: AppRepository = AppRepository(parentApplication)

    fun addExercise(callback: (Long) -> Unit){
        viewModelScope.launch {
             callback(repository.addExercise(Exercise(name = parentApplication.getString(R.string.recordExercise_defaultExerciseName))))
        }
    }
}