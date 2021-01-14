package app.openair.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "exercise")
class Exercise(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    /**
     * The time the start button was pressed
     * This value is not affected by the GPS fix results
     */
    var startTime: Date = Date(),

    /**
     * The time the stop button was pressed
     * This value is not affected by the GPS fix results
     */
    var endTime: Date = Date(),

    /**
     * The user supplied name of the [Exercise]
     */
    var name: String? = null,

    /**
     * The user supplied notes to accompany the [Exercise]
     */
    var notes: String? = null
){

    /**
     * The calculated duration of the exercise in milliseconds
     */
    var duration: Long = 0

    /**
     * The calculated distance of the exercise, in meters, from the [Location]s associated with it
     */
    var distance: Float = 0f

    /**
     * The average speed during the exercise, in meters per second
     */
    var averageSpeed: Float = 0f

    /**
     * The total height climbed during the exercise, in meters
     */
    var elevationGain: Float = 0f
}