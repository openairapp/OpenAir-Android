package app.openair.ui.feature.showSingleExercise

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import app.openair.R
import app.openair.observeOnce
import com.google.android.material.textfield.TextInputEditText

class EditExerciseActivity : AppCompatActivity() {

    companion object {
        const val EXERCISE_ID_EXTRA = "exerciseId"
    }

    var exerciseId: Long? = null
    lateinit var viewModel: EditExerciseViewModel
    lateinit var nameField: TextInputEditText
    lateinit var notesField: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalize_exercise)
        viewModel = ViewModelProvider(this).get(EditExerciseViewModel::class.java)

        // get the current exercise Id from the bundled data
        val bundle = intent.extras
        viewModel.exerciseId = bundle?.getLong(EXERCISE_ID_EXTRA)

        nameField = findViewById(R.id.text_exercise_name)
        notesField = findViewById(R.id.text_exercise_notes)

        // we don't want this to overwrite user changes if the repository were to update
        viewModel.exercise.observeOnce {
            nameField.setText(it.name)
            notesField.setText(it.notes)
        }
    }

    fun onSave(v: View){
        viewModel.saveChanges(
            nameField.text.toString(),
            notesField.text.toString()
        )
        finish()
    }
}