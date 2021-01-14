package app.openair.model.database

import androidx.room.Delete
import androidx.room.Insert

interface BaseDao<T> {
    @Insert
    suspend fun insert(obj: T): Long

    @Insert
    suspend fun insert(objList: List<T>)

    @Delete
    suspend fun delete(obj: T)
}