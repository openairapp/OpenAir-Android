package app.openair.ui.feature.showStats

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import app.openair.R
import app.openair.getSerialized
import app.openair.model.AppRepository
import app.openair.model.logic.TimePeriodStats
import app.openair.putSerialized
import app.openair.types.StatsPeriod
import app.openair.ui.formatter.FormatUtil
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@ExperimentalCoroutinesApi
class ShowStatsViewModel(application: Application) : AndroidViewModel(application) {

    private var repository = AppRepository(application)
    private val statsPeriodKey = application.getString(R.string.shared_preference_default_statsPeriod_key)

    private val sharedPreferenceHandle: SharedPreferences? = application.getSharedPreferences(
        application.getString(R.string.shared_preference_key), Context.MODE_PRIVATE
    )

    // the current time period that we should show stats charts for
    val period: MutableLiveData<StatsPeriod> = MutableLiveData(
        // get default value from shared preferences
        // custom extension to Shared Preference to allow storing enums
        sharedPreferenceHandle?.getSerialized(
            statsPeriodKey,
            StatsPeriod.WEEK
        )
    )

    // this observes the period value, and when it changes, sends a new query to the repository
    // this new live data source is then re-emitted so the change is abstracted away from the view
    val stats: LiveData<TimePeriodStats> = period.switchMap {
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitSource(repository.getTimeSeriesStats(it))
        }
    }

    /**
     * pretty much a setter for period, but not quite since it's LiveData
     * also updates shared preferences
     */
    fun setStatsPeriod(newPeriod: StatsPeriod) {
        period.value = newPeriod

        // custom extension to Shared Preference to allow storing enums
        sharedPreferenceHandle?.putSerialized(statsPeriodKey, newPeriod)
    }

    /**
     * Format the data pairs for the charts
     */
    fun transformPairWithDateToEntry(input: List<Pair<Date, Float>>): List<Entry> =
        input.map { Entry(it.first.time.toFloat(), it.second) }

    fun formatDistance(distance: Float): String =
        FormatUtil.formatMetersToKilometersString(distance)

    fun formatSpeed(speed: Float): String =
        FormatUtil.formatMpsToKphString(speed)

    fun formatDuration(duration: Long): String =
        FormatUtil.formatMillisecondsToString(duration)

    fun formatElevation(elevation: Float): String =
        FormatUtil.formatAltitudeString(elevation)
}