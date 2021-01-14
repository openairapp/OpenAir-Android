package app.openair.ui.feature.recordExercise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.openair.model.AppRepository
import kotlinx.coroutines.launch
import java.util.*

class FinalizeExerciseViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: AppRepository = AppRepository(application)

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            repository.deleteExercise(exerciseId)
        }
    }

    fun addFinishData(exerciseId: Long, name: String, notes: String, endTime: Date) {
        viewModelScope.launch {
            repository.addFinishData(exerciseId, name, notes, endTime)
        }
    }
}