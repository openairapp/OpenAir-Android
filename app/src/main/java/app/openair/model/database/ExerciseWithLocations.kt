package app.openair.model.database

import androidx.room.Embedded
import androidx.room.Relation

data class ExerciseWithLocations(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val locations: List<Location>
)