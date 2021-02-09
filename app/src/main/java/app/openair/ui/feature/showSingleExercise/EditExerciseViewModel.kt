package app.openair.ui.feature.showSingleExercise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.openair.model.AppRepository
import app.openair.model.database.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: AppRepository = AppRepository(application)
    lateinit var exercise: LiveData<Exercise>

    var exerciseId: Long? = null
        set(value) {
            field = value
            viewModelScope.launch {
                exercise = repository.getExercise(value!!)
            }
        }

    fun saveChanges(name: String, notes: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateExercise(exerciseId!!, name, notes)
        }
    }
}