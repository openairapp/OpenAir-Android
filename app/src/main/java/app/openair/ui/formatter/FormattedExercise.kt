package app.openair.ui.formatter

import java.text.SimpleDateFormat
import app.openair.model.database.Exercise
import app.openair.ui.formatter.FormatUtil.Companion.formatAltitudeString
import app.openair.ui.formatter.FormatUtil.Companion.formatMetersToKilometersString
import app.openair.ui.formatter.FormatUtil.Companion.formatMillisecondsToString
import app.openair.ui.formatter.FormatUtil.Companion.formatMpsToKphString

class FormattedExercise(exercise: Exercise) {
    val id: Long = exercise.id
    val name: String = exercise.name.toString()
    val date: String = SimpleDateFormat("d MMMM yyyy, HH:mm").format(exercise.startTime)
    val distance: String = formatMetersToKilometersString(exercise.distance)
    val duration: String = formatMillisecondsToString(exercise.duration)
    val elevation: String = formatAltitudeString(exercise.elevationGain)
    val speed: String = formatMpsToKphString(exercise.averageSpeed)
    val notes: String = if(exercise.notes == null) "" else exercise.notes.toString()
}