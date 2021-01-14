package app.openair.ui.feature.recordExercise

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*

class LocationManager(private val context: Context) {

    companion object {
        val REQUEST_LOCATION_PERMISSION = 100
    }

    lateinit var callback: (result: LocationResult) -> Unit

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.create()?.apply {
        interval = 10000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 10f
    }!!

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            callback(locationResult)
        }
    }

    @SuppressLint("MissingPermission")
    fun startPoll() {
        fusedLocationClient.locationAvailability.addOnSuccessListener { availability ->
            Log.d("OpenAir", "location availability: ${availability.isLocationAvailable}")
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopPoll() {
        Log.d("OpenAir", "stopping location updates")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun verifyAvailability(activity: Activity): Boolean {
        return verifyPlayServices(activity) && verifyLocationPermissions(activity)
    }

    private fun verifyPlayServices(activity: Activity): Boolean {
        val apiInstance = GoogleApiAvailability.getInstance()
        val connectionResult = apiInstance.isGooglePlayServicesAvailable(context)

        if (connectionResult != ConnectionResult.SUCCESS) {
            Log.d("OpenAir", "Google Play Services are unavailable on this device")

            if (apiInstance.isUserResolvableError(connectionResult)) {
                apiInstance.getErrorDialog(activity, connectionResult, 1010).show()
            }
            return false
        }
        Log.d("OpenAir", "Google Play Services are available on this device")
        return true
    }

    private fun verifyLocationPermissions(activity: Activity): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("OpenAir", "no location permissions, requesting from user")
            requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return false
        }
        Log.d("OpenAir", "location permissions already granted")
        return true
    }
}