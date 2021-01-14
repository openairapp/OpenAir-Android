package app.openair.ui.feature.recordExercise

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import app.openair.R
import app.openair.ui.feature.main.MainActivity
import app.openair.ui.feature.recordExercise.FinalizeExerciseActivity.Companion.EXERCISE_ACTION_DELETE
import app.openair.ui.feature.recordExercise.FinalizeExerciseActivity.Companion.EXERCISE_ID_EXTRA

// TODO handle the user pressing back while recording an exercise
//  it should continue to record, display a banner on the main activity, and the new exercise FAB should take you straight to your current recording
class TrackExerciseActivity : AppCompatActivity() {

    companion object {
        const val NOTIFICATION_ACTION_FINISH_EXERCISE = "endExercise"
        const val NOTIFICATION_ACTION_DELETE_EXERCISE = "deleteExercise"
        const val SERVICE_CALLBACK_IDENTIFIER = "TrackExerciseActivity"
    }

    private var recordService: RecordService.MyBinder? = null

    lateinit var timeField: TextView
    lateinit var speedField: TextView
    lateinit var distanceField: TextView
    private lateinit var viewModel: TrackExerciseViewModel
    private var deleteExercise: Boolean = false
    private var endExercise: Boolean = false
    private var exerciseId: Long? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            recordService = service as RecordService.MyBinder

            // don't bother setting up the ui if we're redirecting straight away
            if (endExercise) {
                onStopRecording()
            } else {
                recordService?.registerCallback(SERVICE_CALLBACK_IDENTIFIER, callback)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            recordService?.unregisterCallback(SERVICE_CALLBACK_IDENTIFIER)
            recordService = null
        }
    }

    private val callback = object : ServiceCallback {
        override fun updateCurrentExerciseData(
            speed: Float,
            distance: Float
        ) {
            runOnUiThread {
                speedField.text = viewModel.formatSpeed(speed)
                distanceField.text = viewModel.formatDistance(distance)
            }
        }

        override fun updateCurrentTime(time: Long) {
            runOnUiThread {
                timeField.text = viewModel.formatTime(time)
            }
        }

        override fun onStopTracking() {
            // not used here
        }

        override fun onStartTracking() {
            // not used here
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent?.extras
        // == true since it could also be null
        deleteExercise = extras?.getBoolean(NOTIFICATION_ACTION_DELETE_EXERCISE) == true
        endExercise =
            deleteExercise || extras?.getBoolean(NOTIFICATION_ACTION_FINISH_EXERCISE) == true

        this.bindService(
            Intent(this, RecordService::class.java),
            serviceConnection,
            Context.BIND_ABOVE_CLIENT
        )

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateUpTo(Intent(this@TrackExerciseActivity, MainActivity::class.java))
            }
        }
        callback.isEnabled = true
        this.onBackPressedDispatcher.addCallback(callback)


        // don't bother setting up the ui if we're redirecting straight away
        if (!endExercise) {
            setContentView(R.layout.activity_track_exercise)

            viewModel = ViewModelProvider(this).get(TrackExerciseViewModel::class.java)

            timeField = findViewById(R.id.current_time)
            speedField = findViewById(R.id.current_speed)
            distanceField = findViewById(R.id.current_distance)
        }
    }

    fun onStopRecording(v: View) = onStopRecording()

    private fun onStopRecording() {
        // store exerciseId in case the user goes back into this activity after leaving
        if (exerciseId == null) {
            exerciseId = recordService?.stopTracking()!!
        }

        val activityIntent = Intent(this, FinalizeExerciseActivity::class.java)
        activityIntent.putExtra(EXERCISE_ID_EXTRA, exerciseId)
        activityIntent.putExtra(EXERCISE_ACTION_DELETE, deleteExercise)

        startActivity(activityIntent)
    }
}