package app.openair.model.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ExerciseDao : BaseDao<Exercise> {

    @Query("SELECT * FROM `exercise` ORDER BY startTime DESC")
    fun getAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM `exercise` WHERE exercise.id = :id")
    fun get(id: Long): Flow<Exercise>

    @Query("SELECT * FROM `exercise` WHERE exercise.id = :id")
    fun getWithLocations(id: Long): Flow<ExerciseWithLocations>

    @Query("SELECT * FROM `exercise` WHERE exercise.endTime > :fromDate AND exercise.endTime <= :toDate ORDER BY startTime ASC")
    fun getBetween(fromDate: Date, toDate: Date): Flow<List<Exercise>>

    /**
     * the same a get(), but without live data
     */
    @Query("SELECT * FROM `exercise` WHERE exercise.id = :id")
    fun getStatic(id: Long): ExerciseWithLocations

    @Query("DELETE FROM `exercise`")
    fun deleteAll()

    @Query("DELETE FROM `exercise` WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM `exercise` WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query(
        """
        UPDATE `exercise` 
        SET 
            name = :name, 
            notes = :notes,
            endTime = :endTime
        WHERE id = :exerciseId
    ;"""
    )
    suspend fun addFinishData(
        exerciseId: Long,
        name: String,
        notes: String,
        endTime: Date
    )

    @Query(
        """
        UPDATE `exercise` 
        SET 
            name = :name, 
            notes = :notes
        WHERE id = :exerciseId
    ;"""
    )
    suspend fun update(
        exerciseId: Long,
        name: String,
        notes: String
    )

    @Query(
        """
        UPDATE `exercise` 
        SET 
            duration = :duration, 
            distance = :distance,
            averageSpeed = :averageSpeed,
            elevationGain = :elevationGain 
        WHERE id = :exerciseId
    ;"""
    )
    suspend fun addCalculatedValues(
        exerciseId: Long,
        duration: Long,
        distance: Float,
        averageSpeed: Float,
        elevationGain: Float
    )
}