package app.openair.ui.feature.showStats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.openair.R
import app.openair.observeOnce
import app.openair.types.StatsPeriod
import app.openair.ui.component.Charts
import com.github.mikephil.charting.charts.LineChart
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ShowStatsFragment : Fragment() {

    companion object {
        fun newInstance() = ShowStatsFragment()
    }

    private lateinit var viewModel: ShowStatsViewModel
    private var charts = arrayOfNulls<LineChart>(4)

    private val periodButtonMapping: Map<Int, StatsPeriod> = mapOf(
        R.id.radio_periodSelector_day to StatsPeriod.DAY,
        R.id.radio_periodSelector_week to StatsPeriod.WEEK,
        R.id.radio_periodSelector_month to StatsPeriod.MONTH,
        R.id.radio_periodSelector_year to StatsPeriod.YEAR
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(ShowStatsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set the onclick callback for each of the radio buttons
        periodButtonMapping.forEach {
            view.findViewById<RadioButton>(it.key)
                ?.setOnClickListener(::onPeriodSelected)
        }

        // custom extension to liveData
        // this only needs to run once to set the initial value, then the RadioGroup manages checked state
        viewModel.period.observeOnce{period ->
            // get the button id for a given StatsPeriod
            val buttonId = periodButtonMapping.filterValues { it == period }
                .keys
                .elementAt(0)

            // set that button to checked
            view.findViewById<RadioButton>(buttonId)
                ?.isChecked = true
        }

        val totalDistanceTextView = view.findViewById<TextView>(R.id.text_total_distance)
        val totalDurationTextView = view.findViewById<TextView>(R.id.text_total_duration)
        val totalElevationGainTextView = view.findViewById<TextView>(R.id.total_elevationGain)
        val averageSpeedTextView = view.findViewById<TextView>(R.id.text_average_speed)

        charts[0] = view.findViewById(R.id.chart_distance)
        charts[1] = view.findViewById(R.id.chart_duration)
        charts[2] = view.findViewById(R.id.chart_elevationGain)
        charts[3] = view.findViewById(R.id.chart_averageSpeed)

        // update the charts when the data changes
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            // update the total numbers
            totalDistanceTextView?.text = viewModel.formatDistance(stats.distanceTotal)
            totalDurationTextView?.text = viewModel.formatDuration(stats.durationTotal)
            totalElevationGainTextView?.text = viewModel.formatElevation(stats.elevationTotal)
            averageSpeedTextView?.text = viewModel.formatSpeed(stats.speedAverage)


            // update the chart data
            charts[0]?.let { chart ->
                Charts.setupDistanceChart(
                    chart,
                    viewModel.transformPairWithDateToEntry(stats.distance),
                    stats::timePeriodFormatter,
                    alignX = true
                )
            }

            charts[1]?.let { chart ->
                Charts.setupDurationChart(
                    chart,
                    viewModel.transformPairWithDateToEntry(stats.duration),
                    stats::timePeriodFormatter,
                    alignX = true
                )
            }

            charts[2]?.let { chart ->
                Charts.setupElevationChart(
                    chart,
                    viewModel.transformPairWithDateToEntry(stats.elevation),
                    stats::timePeriodFormatter,
                    alignX = true,
                    absoluteY = true
                )
            }

            charts[3]?.let { chart ->
                Charts.setupSpeedChart(
                    chart,
                    viewModel.transformPairWithDateToEntry(stats.speed),
                    stats::timePeriodFormatter,
                    alignX = true
                )
            }
        }
    }

    private fun onPeriodSelected(view: View) {
        if (view is RadioButton && view.isChecked) {
            // tell the ViewModel which button was pressed
            periodButtonMapping[view.getId()]
                ?.let { viewModel.setStatsPeriod(it) }
        }
    }
}