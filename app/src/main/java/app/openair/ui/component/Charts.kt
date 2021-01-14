package app.openair.ui.component

import android.graphics.Color
import app.openair.ui.formatter.FormatUtil.Companion.formatAltitudeString
import app.openair.ui.formatter.FormatUtil.Companion.formatMetersToKilometersString
import app.openair.ui.formatter.FormatUtil.Companion.formatMillisecondsToString
import app.openair.ui.formatter.FormatUtil.Companion.formatMpsToKphString
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class Charts {
    companion object {
        private val colors = intArrayOf(
            Color.rgb(137, 230, 81),
            Color.rgb(89, 199, 250),
            Color.rgb(240, 100, 80),
            Color.rgb(240, 80, 240),
        )

        private fun timeFormatter(value: Float): String {
            return formatMillisecondsToString(value.toLong())
        }

        fun setupElevationChart(
            chart: LineChart,
            data: List<Entry>,
            xFormatter: (Float) -> String = ::timeFormatter,
            alignX: Boolean = false,
            absoluteY: Boolean = false
        ) {
            setupOrUpdateChart(
                chart,
                data,
                GenericFormatter(xFormatter),
                GenericFormatter(::formatAltitudeString),
                colors[0],
                alignX,
                absoluteY
            )
        }

        fun setupSpeedChart(
            chart: LineChart,
            data: List<Entry>,
            xFormatter: (Float) -> String = ::timeFormatter,
            alignX: Boolean = false
        ) {
            setupOrUpdateChart(
                chart,
                data,
                GenericFormatter(xFormatter),
                GenericFormatter(::formatMpsToKphString),
                colors[1],
                alignX
            )
        }

        fun setupDistanceChart(
            chart: LineChart,
            data: List<Entry>,
            xFormatter: (Float) -> String = ::timeFormatter,
            alignX: Boolean = false
        ) {
            setupOrUpdateChart(
                chart,
                data,
                GenericFormatter(xFormatter),
                GenericFormatter(::formatMetersToKilometersString),
                colors[2],
                alignX
            )
        }

        fun setupDurationChart(
            chart: LineChart,
            data: List<Entry>,
            xFormatter: (Float) -> String = ::timeFormatter,
            alignX: Boolean = false,
        ) {
            setupOrUpdateChart(
                chart,
                data,
                GenericFormatter(xFormatter),
                GenericFormatter(fun(input: Float): String {return formatMillisecondsToString(input.toLong())}),
                colors[3],
                alignX
            )
        }

        // from https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/CubicLineChartActivity.java
        // under Apache license version 2.0
        // with modifications
        private fun setupOrUpdateChart(
            chart: LineChart,
            data: List<Entry>,
            xFormatter: ValueFormatter,
            yFormatter: ValueFormatter,
            color: Int,
            alignX: Boolean,
            absoluteY: Boolean = true
        ) {
            if (chart.data != null && chart.data.dataSetCount > 0) {
                // update data set if it already exists
                val dataSet: LineDataSet = chart.data.getDataSetByIndex(0) as LineDataSet
                dataSet.values = data
                chart.data.notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                // create a data set and give it a type
                val dataSet = LineDataSet(data, "DataSet 1")

                // sets the shape characteristics of the graph
                dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

                // shades the area under the graph
                dataSet.setDrawFilled(true)

                // prevents dots on each data point
                dataSet.setDrawCircles(false)

                // sets the colour of the line
                dataSet.color = color

                // and the color of the area under the graph
                dataSet.fillColor = color

                // sets the colour of the grid that appears when you tap on the graph
                dataSet.highLightColor = Color.RED

                // stops the numbers being drawn over the graph
                dataSet.setDrawValues(false)

                // add data
                chart.data = LineData(dataSet)

                // hide description text
                chart.description.isEnabled = false

                // enable touch gestures
                chart.setTouchEnabled(true)
                // enable scaling and dragging
                chart.isDragEnabled = true
                chart.setScaleEnabled(true)

                // get the legend (only possible after setting data)
                chart.legend.isEnabled = false
                chart.axisRight.isEnabled = false
                chart.xAxis.isEnabled = true

                // FIXME this doesn't work with dark theme
                // set the colour of the left axis labels
                chart.axisLeft.textColor = Color.BLACK

                // set the axis labels to overlap the chart rather than be outside it
                chart.axisLeft.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)

                // draw horizontal lines from the left x axis
                chart.axisLeft.setDrawGridLines(true)
            }

            // pass in formatter for axis labels
            chart.xAxis.valueFormatter = xFormatter
            chart.axisLeft.valueFormatter = yFormatter


            if(alignX){
                // this ensures the data line up nicely without having too many points
                var divisor = 1
                while((data.size / divisor) > 15){
                    divisor++
                }
                // how many axis labels to show
                chart.xAxis.setLabelCount(data.size / divisor, true)
            }

            if(absoluteY) {
                chart.axisLeft.axisMinimum = 0f // start at zero
            }

            // make the chart re-render
            chart.invalidate()
        }
    }

    class GenericFormatter(val formatter: (Float) -> String) : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return formatter(value)
        }
    }
}