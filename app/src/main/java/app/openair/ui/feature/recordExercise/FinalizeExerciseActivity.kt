package app.openair.ui.feature.recordExercise

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import app.openair.R
import app.openair.ui.feature.main.MainActivity
import com.google.android.material.textfield.TextInputEditText
import java.util.*


class FinalizeExerciseActivity : AppCompatActivity() {

    private lateinit var viewModel: FinalizeExerciseViewModel
    private lateinit var nameEditText: TextInputEditText
    private lateinit var notesEditText: TextInputEditText

    private var exerciseId: Long? = null
    private lateinit var stopTime: Date

    companion object {
        const val EXERCISE_ID_EXTRA = "exerciseId"
        const val EXERCISE_ACTION_DELETE = "deleteExercise"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // take the current time to use as the time the exercise ended
        stopTime = Date()

        val extras = intent?.extras
        exerciseId = extras?.getLong(EXERCISE_ID_EXTRA)
        val deleteExercise = extras?.getBoolean(EXERCISE_ACTION_DELETE) == true

        viewModel = ViewModelProvider(this).get(FinalizeExerciseViewModel::class.java)

        if(deleteExercise){
            deleteExercise()
        }else {
            setContentView(R.layout.activity_finalize_exercise)

            nameEditText = findViewById(R.id.exercise_name_EditText)
            notesEditText = findViewById(R.id.exercise_notes_EditText)
        }
    }

    /**
     * add a custom menu with a delete button
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.finalize_activity_menu, menu)
        return true
    }

    /**
     * listen to events on the custom menu
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_delete -> {
            deleteExercise()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun deleteExercise() {
        viewModel.deleteExercise(exerciseId!!)
        goBackHome()
    }

    /**
     * save the current exercise with the data provided by the user
     */
    fun onSave(v: View) {
        viewModel.addFinishData(
            exerciseId!!,
            nameEditText.text.toString(),
            notesEditText.text.toString(),
            stopTime
        )
        goBackHome()
    }

    private fun goBackHome() {
        navigateUpTo(Intent(this, MainActivity::class.java))
    }
}