package app.openair.ui.feature.recordExercise

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import app.openair.R
import app.openair.getStringResourceByName
import app.openair.model.AppRepository
import app.openair.model.database.Location
import app.openair.ui.feature.recordExercise.TrackExerciseActivity.Companion.NOTIFICATION_ACTION_DELETE_EXERCISE
import app.openair.ui.feature.recordExercise.TrackExerciseActivity.Companion.NOTIFICATION_ACTION_FINISH_EXERCISE
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import android.location.Location as AndroidLocation

// This has no support for pausing, or handling loss of location information nicely
// TODO consider whether this service should be in the model
class RecordService : Service() {

    companion object {
        const val MINIMUM_FIX_ACCURACY = 100
        const val NOTIFICATION_CHANNEL_ID = "1001"
        const val NOTIFICATION_ID = 1
        const val EXERCISE_ID_EXTRA = "exerciseId"
        var running = false
    }

    private val binder = MyBinder()
    private lateinit var repository: AppRepository
    private lateinit var locationManager: LocationManager

    private var exerciseId: Long? = null
    private var secondsSinceStart: Long = 0
    private var timer: Timer? = null
    private var lastPosition: AndroidLocation? = null
    private var distanceTotal: Float = 0f

    var callbacks: MutableMap<String, ServiceCallback> = mutableMapOf()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        running = true
        // grab the id of the exercise we've just started
        val extras = intent?.extras

        exerciseId = extras?.getLong(EXERCISE_ID_EXTRA)

        // START FOREGROUND
        startForeground(NOTIFICATION_ID, buildNotification())

        repository = AppRepository(this)

        // start receiving location updates
        locationManager = LocationManager(this)
        locationManager.callback = ::locationCallback
        locationManager.startPoll()

        // set up the timer to show the user
        timer = Timer()
        timer?.scheduleAtFixedRate(
            MyTimerTask(),
            0,
            1000
        )

        // notify bound clients that we've started tracking
        callbacks.forEach { it.value.onStartTracking() }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        running = false
    }

    private fun buildNotification(): Notification {
        val mainIntent: PendingIntent =
            Intent(this, TrackExerciseActivity::class.java)
                // prevent the intent starting a new activity if we're already on it
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .let { notificationIntent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                }

        val finishIntent: PendingIntent =
            Intent(this, TrackExerciseActivity::class.java)
                .also { notificationIntent ->
                    // tell it to stop the exercise
                    notificationIntent.putExtra(NOTIFICATION_ACTION_FINISH_EXERCISE, true)
                }
                .let { notificationIntent ->
                    PendingIntent.getActivity(
                        this,
                        // request code must be unique for each intent or the previous one won't work
                        1,
                        notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                }

        val deleteIntent: PendingIntent =
            Intent(this, TrackExerciseActivity::class.java)
                .also { notificationIntent ->
                    // tell it to delete the exercise
                    notificationIntent.putExtra(NOTIFICATION_ACTION_DELETE_EXERCISE, true)
                }
                .let { notificationIntent ->
                    PendingIntent.getActivity(
                        this,
                        2,
                        notificationIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                    )
                }

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getStringResourceByName("recordExercise_notification_channelName"),
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getStringResourceByName("recordExercise_notification_title"))
            .setContentIntent(mainIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.stop_black_24dp,
                getStringResourceByName("recordExercise_notificationAction_finish"),
                finishIntent
            )
            .addAction(
                R.drawable.delete_24dp,
                getStringResourceByName("recordExercise_notificationAction_delete"),
                deleteIntent
            )
            .build()
    }

    inner class MyBinder : Binder() {

        fun stopTracking(): Long {
            running = false
            val currentExerciseId = exerciseId
            exerciseId = null

            locationManager.stopPoll()

            timer?.cancel()
            secondsSinceStart = 0

            // notify bound clients that we're no longer tracking
            callbacks.forEach { it.value.onStopTracking() }

            stopForeground(true)
            stopSelf()

            return currentExerciseId!!
        }

        fun registerCallback(callbackIdentifier: String, serviceCallback: ServiceCallback) {
            callbacks[callbackIdentifier] = serviceCallback
        }

        fun unregisterCallback(callbackIdentifier: String) {
            callbacks.remove(callbackIdentifier)
        }

        fun getIsInProgress(): Boolean {
            return running
        }
    }

    inner class MyTimerTask : TimerTask() {
        override fun run() {
            callbacks.forEach { it.value.updateCurrentTime(secondsSinceStart) }
            secondsSinceStart += 1
        }
    }

    private fun locationCallback(locationResult: LocationResult?) {
        locationResult ?: return
        Log.v("g53mdp", "location update: ${locationResult.lastLocation}")

        // discard any bad readings
        val filteredLocations = locationResult.locations
            .filter { it.accuracy <= MINIMUM_FIX_ACCURACY }

        // process location for ui updates
        filteredLocations.forEach {
            if (lastPosition != null) {
                // update our distance counter with the current recorded distance
                val resultArray = FloatArray(1)
                AndroidLocation.distanceBetween(
                    lastPosition!!.latitude,
                    lastPosition!!.longitude,
                    it.latitude,
                    it.longitude,
                    resultArray
                )
                distanceTotal += resultArray[0]
            }
            lastPosition = it
        }
        // push data back to ui
        callbacks.forEach { it.value.updateCurrentExerciseData(locationResult.lastLocation.speed, distanceTotal) }

        //process location for database records
        val locationList = filteredLocations.map {
            Location(
                exerciseId!!,
                it.latitude,
                it.longitude,
                it.altitude,
                it.speed,
                it.time
            )
        }
        // write to the database in a different thread for IO
        CoroutineScope(Dispatchers.IO).launch {
            repository.logLocation(locationList)
        }
    }
}