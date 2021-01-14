package app.openair.ui.formatter

class FormatUtil{
    companion object{
        fun formatMillisecondsToString(time: Long): String = formatSecondsToString(time / 1000)

        fun formatSecondsToString(time: Long): String {
            val hours = (time / (60 * 60))
            val minutes = (time / 60).rem(60)
            val seconds = time.rem(60)

            return String.format(
                "%01d:%02d:%02d",
                hours, minutes, seconds
            )
        }

        fun formatAltitudeString(altitude: Float): String = formatAltitudeString(altitude.toDouble())

        fun formatAltitudeString(altitude: Double): String = String.format("%.1f m", altitude)

        fun formatMpsToKphString(speed: Float): String {
            val kph = speed * 3.6
            return String.format("%.1f km/h", kph)
        }

        fun formatMetersToKilometersString(distance: Float): String {
            val kilometers = distance / 1000
            return String.format("%.2f km", kilometers)
        }
    }
}