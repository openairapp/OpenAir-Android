package app.openair.ui.feature.showSingleExercise

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import app.openair.R
import app.openair.ui.component.Charts.Companion.setupElevationChart
import app.openair.ui.component.Charts.Companion.setupSpeedChart
import app.openair.ui.component.MapHelper
import com.github.mikephil.charting.charts.LineChart
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import timber.log.Timber


class ShowExerciseDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXERCISE_ID_EXTRA = "exerciseId"
        const val EXERCISE_ID_SAVE_INSTANCE = "exerciseId"
        const val CREATE_FILE = 5050
    }

    var exerciseId: Long? = null
    lateinit var viewModel: ShowExerciseDetailsViewModel
    private lateinit var map: GoogleMap
    private lateinit var mapHelper: MapHelper
    private var charts = arrayOfNulls<LineChart>(2)
    private var exerciseName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_exercise_details)
        viewModel = ViewModelProvider(this).get(ShowExerciseDetailsViewModel::class.java)


        // get the current exercise Id from the bundled data
        val bundle = intent.extras
        exerciseId = bundle?.getLong(EXERCISE_ID_EXTRA)
        viewModel.setExerciseId(exerciseId!!)


        // set up exercise data fields
        val dateTextView = findViewById<TextView>(R.id.text_exercise_date)
        val nameTextView = findViewById<TextView>(R.id.text_exercise_name)
        val distanceTextView = findViewById<TextView>(R.id.text_exercise_distance)
        val durationTextView = findViewById<TextView>(R.id.text_exercise_duration)
        val elevationTextView = findViewById<TextView>(R.id.text_exercise_elevationGain)
        val speedTextView = findViewById<TextView>(R.id.text_exercise_speed)
        val notesTextView = findViewById<TextView>(R.id.exercise_notes)

        viewModel.formattedExercise.observe(this, {
            // update the ui to show the exercise data
            dateTextView.text = it.date
            nameTextView.text = it.name
            distanceTextView.text = it.distance
            durationTextView.text = it.duration
            elevationTextView.text = it.elevation
            speedTextView.text = it.speed
            notesTextView.text = it.notes

            // also update the values used to export the data
            exerciseName = "${it.date}-${it.name}.gpx"
        })

        // set up the google map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // set up charts
        charts[0] = findViewById(R.id.chart_elevation)
        charts[1] = findViewById(R.id.chart_speed)

        viewModel.chartPoints.observe(this, { data ->
            //check they're not null and pass in the data
            charts[0]?.let {
                setupElevationChart(
                    it,
                    data[ShowExerciseDetailsViewModel.CHART_POINT_SERIES_ELEVATION]
                )
            }

            charts[1]?.let {
                setupSpeedChart(
                    it,
                    data[ShowExerciseDetailsViewModel.CHART_POINT_SERIES_SPEED]
                )
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putLong(EXERCISE_ID_SAVE_INSTANCE, exerciseId!!)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        exerciseId = savedInstanceState.getLong(EXERCISE_ID_SAVE_INSTANCE)
    }

    /**
     * add a custom menu with a delete button
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.exercise_details, menu)
        return true
    }

    /**
     * listen to events on the custom menu
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            val intent = Intent(this, EditExerciseActivity::class.java).apply {
                putExtra(EXERCISE_ID_EXTRA, exerciseId)
            }
            startActivity(intent)
            true
        }
        R.id.action_export -> {
            // I tried to do this the new way but I couldn't figure out how to set the mime type properly
            // request a uri to save file
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/gpx+xml"
                putExtra(Intent.EXTRA_TITLE, exerciseName)
            }
            startActivityForResult(intent, CREATE_FILE)

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    viewModel.exportGPX(exerciseId!!, it)
                }
            } else {
                Timber.d("File export canceled by user")
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnMapLoadedCallback(::mapLoadedCallback)

        // get the size of the screen for padding the map route
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        mapHelper = MapHelper(this, map, displayMetrics.widthPixels)
    }

    private fun mapLoadedCallback() {
        // generate map points for google maps
        // do it here so we know the map is ready
        viewModel.mapPoints.observe(this, {
            mapHelper.updateMapRoute(it)
        })
    }


}