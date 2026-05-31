package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShredDao {
    @Query("SELECT * FROM shred_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<ShredHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(record: ShredHistory)

    @Query("DELETE FROM shred_history")
    suspend fun clearAllHistory()
}
