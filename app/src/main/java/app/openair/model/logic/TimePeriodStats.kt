package app.openair.model.logic

import androidx.core.os.LocaleListCompat
import app.openair.model.database.Exercise
import app.openair.types.StatsPeriod
import java.text.SimpleDateFormat
import java.util.*

class TimePeriodStats(private val period: StatsPeriod) {

    // for all of these, the date is represented in unix milliseconds
    lateinit var distance: List<Pair<Date, Float>>
    lateinit var speed: List<Pair<Date, Float>>
    lateinit var elevation: List<Pair<Date, Float>>
    lateinit var duration: List<Pair<Date, Float>>

    var distanceTotal: Float = 0f
    var speedAverage: Float = 0f
    var elevationTotal: Float = 0f
    var durationTotal: Long = 0L

    /**
     * this can be passed into the charts to generate the x axis labels
     */
    fun timePeriodFormatter(date: Float): String {
        val pattern = when (period) {
            StatsPeriod.DAY -> "h a" // 05 AM
            StatsPeriod.WEEK -> "EEE" // Tue
            StatsPeriod.MONTH -> "d" // 31
            StatsPeriod.YEAR -> "MMM" // Jul
        }

        // get the date string using the date using the devices default Locale
        return SimpleDateFormat(pattern, LocaleListCompat.getDefault().get(0))
            .format(Date(date.toLong()))
    }

    /**
     * A function which increments the date by an amount suitable for chunking based on the given period
     */
    private fun incrementDate(input: Date): Date {
        val incrementPeriod = when (period) {
            StatsPeriod.DAY -> Calendar.HOUR
            StatsPeriod.WEEK,
            StatsPeriod.MONTH -> Calendar.DAY_OF_YEAR
            StatsPeriod.YEAR -> Calendar.MONTH
        }
        val calendar = Calendar.getInstance()
        calendar.time = input
        calendar.add(incrementPeriod, 1)
        return calendar.time
    }


    fun setData(exercises: List<Exercise>, chunkStartDate: Date, chunkEndDate: Date) {

        exercises.let {
            speedAverage = exercises
                .map { it.averageSpeed }
                .filter { it > 0 }
                .average().toFloat()

            distanceTotal = exercises
                .map { it.distance }
                .sum()

            elevationTotal = exercises
                .map { it.elevationGain }
                .sum()

            durationTotal = exercises
                .map { it.duration }
                .sum()
        }

        chunkExercises(exercises, chunkStartDate, chunkEndDate).let {
            speed = avgChunkSpeed(it)
            distance = totalChunkDistance(it)
            elevation = totalChunkElevationGain(it)
            duration = totalChunkDuration(it)
        }
    }



    private fun chunkExercises(
        exercises: List<Exercise>,
        startDate: Date,
        endDate: Date
    ): List<Pair<Date, List<Exercise>>> {
        val result: MutableList<Pair<Date, List<Exercise>>> = mutableListOf()

        var currentStart = startDate
        var currentEnd = incrementDate(startDate)

        do {
            // we're only gonna consider end times for this to keep it simple
            // endTime also means we won't include any activity which is currently in progress
            val exercisesInPeriod =
                exercises.filter { it.endTime > currentStart && it.endTime <= currentEnd }

            result.add(
                Pair(
                    // This date is used to align the date/time on the graph when displayed to the user
                    // it makes more sense when using the end of the period
                    // create a new date object so we don't make a mess
                    Date(currentEnd.time),
                    exercisesInPeriod
                )
            )

            currentStart = currentEnd
            currentEnd = incrementDate(currentStart)
        } while (currentEnd.time <= endDate.time)

        return result
    }

    /**
     * Average speed of all the exercises in each chunk
     *
     * this averages the exercise regardless of it's duration
     * that means short exercises are over-represented
     */
    private fun avgChunkSpeed(chunks: List<Pair<Date, List<Exercise>>>): List<Pair<Date, Float>> =
        chunks.map { chunk ->
            Pair(
                // pass the date straight through
                chunk.first,
                // average the average speeds where they're non 0
                (chunk
                    .second
                    .map { it.averageSpeed }
                    // clear zero values so they don't skew the average
                    .filter { it > 0 }
                    .average()
                    // default to 0 if the result is NaN
                    .takeIf { !it.isNaN() } ?: 0)
                    .toFloat()
            )
        }

    /**
     * Total distance of all the exercises in each chunk
     */
    private fun totalChunkDistance(chunks: List<Pair<Date, List<Exercise>>>): List<Pair<Date, Float>> =
        totalChunk(chunks) { it.distance }

    /**
     * Total time of all the exercises in each chunk
     */
    private fun totalChunkDuration(chunks: List<Pair<Date, List<Exercise>>>): List<Pair<Date, Float>> =
        totalChunk(chunks) { it.duration.toFloat() }

    /**
     * Total elevation gain of all the exercises in each chunk
     */
    private fun totalChunkElevationGain(chunks: List<Pair<Date, List<Exercise>>>): List<Pair<Date, Float>> =
        totalChunk(chunks) { it.elevationGain }

    /**
     * Total the given mapped value of all the exercises in each chunk
     */
    private fun totalChunk(
        chunks: List<Pair<Date, List<Exercise>>>,
        mapFun: (Exercise) -> Float
    ): List<Pair<Date, Float>> =
        chunks.map { chunk ->
            Pair(
                // pass the date straight through
                chunk.first,
                // average the average speeds where they're non 0
                (chunk
                    .second
                    .map(mapFun)
                    .sum()
                    // default to 0 if the result is NaN
                    .takeIf { !it.isNaN() } ?: 0)
                    .toFloat()
            )
        }
}