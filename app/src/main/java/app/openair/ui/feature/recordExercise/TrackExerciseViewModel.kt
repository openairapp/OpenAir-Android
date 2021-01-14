package app.openair.ui.feature.recordExercise

import androidx.lifecycle.ViewModel
import app.openair.ui.formatter.FormatUtil.Companion.formatMetersToKilometersString
import app.openair.ui.formatter.FormatUtil.Companion.formatMpsToKphString
import app.openair.ui.formatter.FormatUtil.Companion.formatSecondsToString

class TrackExerciseViewModel : ViewModel() {
    fun formatTime(time: Long): String = formatSecondsToString(time)

    fun formatSpeed(speed: Float): String = formatMpsToKphString(speed)

    fun formatDistance(distance: Float): String = formatMetersToKilometersString(distance)
}