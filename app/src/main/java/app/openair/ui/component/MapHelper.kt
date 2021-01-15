package app.openair.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import app.openair.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

class MapHelper(val context: Context, val map: GoogleMap, private val screenWidth: Int) {

    init {
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
    }

    fun updateMapRoute(points: Array<LatLng>) {
            Log.d("OpenAir", "map initialised, plotting route")
            // clear any previous path since this method may be called multiple times
            map.clear()

            // if there are no points to add, don't continue
            if (points.isEmpty()) {
                return
            }

            // main map line
            map.addPolyline(
                PolylineOptions()
                    .add(*points) // * is Kotlin's spread operator, no pointers here
                    .color(ContextCompat.getColor(context, R.color.primaryColor))
                    .startCap(RoundCap())
                    .endCap(RoundCap())
            )

            // route start marker
            map.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                        points[0].latitude,
                        points[0].longitude
                    )
                    )
                    .title(context.getString(R.string.showSingleExercise_map_marker_start))
                    .icon(getBitmapDescriptorFromVector(R.drawable.play_circle_green_24dp))

            )

            // route end marker
            map.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                        points[points.size -1].latitude,
                        points[points.size -1].longitude
                    )
                    )
                    .title(context.getString(R.string.showSingleExercise_map_marker_end))
                    .icon(getBitmapDescriptorFromVector(R.drawable.stop_circle_red_24dp))
            )

            setMapBounds(points)
            // TODO add start and end markers so it's easier to see where you started
    }

    private fun getBitmapDescriptorFromVector(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor? {

        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val bitmap = Bitmap.createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun setMapBounds(points: Array<LatLng>) {
        if (points.isEmpty()) {
            return
        }

        // initialise with the first point, since we may not overlap (0,0) so we can't use that
        var maxLat: Double = points[0].latitude
        var minLat: Double = points[0].latitude
        var maxLon: Double = points[0].longitude
        var minLon: Double = points[0].longitude

        points.forEach {
            if (it.latitude > maxLat) {
                maxLat = it.latitude
            }

            if (it.latitude < minLat) {
                minLat = it.latitude
            }

            if (it.longitude > maxLon) {
                maxLon = it.longitude
            }

            if (it.longitude < minLon) {
                minLon = it.longitude
            }
        }

        val routeBounds = LatLngBounds(
            LatLng(minLat, minLon), // SW bounds
            LatLng(maxLat, maxLon) // NE bounds
        )

        val mapPadding = screenWidth / 10

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds, mapPadding))
    }
}