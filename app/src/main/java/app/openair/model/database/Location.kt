package app.openair.model.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location",
    foreignKeys = [ForeignKey(
        entity = Exercise::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("exerciseId"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["exerciseId"])]
)
class Location(
    /**
     * Foreign key stating which [Exercise] this [Location] is part of
     */
    var exerciseId: Long,

    /**
     * Latitude of the [Location] in degrees
     */
    var latitude: Double,

    /**
     * Longitude of the [Location] in degrees
     */
    var longitude: Double,

    /**
     * The elevation of the [Location] in meters
     */
    var elevation: Double,

    /**
     * The instantaneous of the [Location] in meters per second
     */
    var speed: Float,

    /**
     * The time the fix of the [Location] was taken, in milliseconds since unix epoch
     */
    var time: Long,
) {

    @PrimaryKey(autoGenerate = true)
    var id = 0
}