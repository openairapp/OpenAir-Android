package app.openair.ui.feature.recordExercise

interface ServiceCallback {
    fun updateCurrentExerciseData(
        speed: Float,
        distance: Float
    )

    fun updateCurrentTime(
        time: Long
    )

    fun onStartTracking()

    fun onStopTracking()
}