package app.openair.ui.formatter

import app.openair.model.database.Exercise
import app.openair.ui.formatter.FormatUtil.Companion.formatAltitudeString
import app.openair.ui.formatter.FormatUtil.Companion.formatMetersToKilometersString
import app.openair.ui.formatter.FormatUtil.Companion.formatMillisecondsToString
import app.openair.ui.formatter.FormatUtil.Companion.formatMpsToKphString

class FormattedExercise(exercise: Exercise) {
    val id: Long = exercise.id
    val name: String
    val date: String
    val distance: String
    val duration: String
    val elevation: String
    val speed: String
    val notes: String
    
    init {
        name = exercise.name.toString()
        // TODO make this a nicer date format
        date = exercise.startTime.toString()
        distance = formatMetersToKilometersString(exercise.distance)
        duration = formatMillisecondsToString(exercise.duration)
        elevation = formatAltitudeString(exercise.elevationGain)
        speed = formatMpsToKphString(exercise.averageSpeed)
        notes = if(exercise.notes == null) "" else exercise.notes.toString()
    }
}