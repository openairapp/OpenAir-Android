package app.openair.ui.feature.recordExercise

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import app.openair.R
import app.openair.ui.feature.recordExercise.RecordService.Companion.EXERCISE_ID_EXTRA

class SetupExerciseActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

    lateinit var startButton: Button
    private lateinit var locationManager: LocationManager
    private lateinit var viewModel: SetupExerciseViewModel

    var locationAvailable = false
        set(value) {
            field = value
            // disable the button if location isn't available
            if (this::startButton.isInitialized) {
                startButton.isEnabled = value
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(SetupExerciseViewModel::class.java)

        setContentView(R.layout.activity_setup_exercise)
        startButton = findViewById(R.id.button_start)

        // see if we have play services and location permissions, and request them if we don't
        locationManager = LocationManager(this)
        locationAvailable = locationManager.verifyAvailability(this)
    }

    /**
     * Called when the user has clicked the "start recording" button
     */
    fun onStartRecording(v: View) {
        // add a new exercise to the database so we can log locations against it
        viewModel.addExercise(::addExerciseCallback)

        // move to the activity to display current exercise progress
        val activityIntent = Intent(this, TrackExerciseActivity::class.java)
        startActivity(activityIntent)
    }

    /**
     * Called when the exercise has been added to the database and we have its id
     */
    private fun addExerciseCallback(exerciseId: Long){
        // start the location logging service
        val serviceIntent = Intent(this, RecordService::class.java)
        // also provide it with the exercise id so it can log locations
        serviceIntent.putExtra(EXERCISE_ID_EXTRA, exerciseId)
        startService(serviceIntent)
    }

    /**
     * This gets called if we ask a user for permissions and they interact with the dialog provided by the OS
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d("OpenAir", "permission response received")
        // if the permission request gets a response, update the button to match
        // if it is granted this should enable the button and allow the user to continue to tracking their position
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationManager.REQUEST_LOCATION_PERMISSION) {
            val locationIndex = permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)

            // Check the response actually contains the Location permission, then set it
            if (locationIndex != -1) {

                val permissionResult =
                    (grantResults[locationIndex] == android.content.pm.PackageManager.PERMISSION_GRANTED)
                locationAvailable = permissionResult

                Log.d(
                    "OpenAir",
                    if (permissionResult) "location permission granted by user :)" else "location permissions denied by user >:("
                )
            }
        }
    }
}