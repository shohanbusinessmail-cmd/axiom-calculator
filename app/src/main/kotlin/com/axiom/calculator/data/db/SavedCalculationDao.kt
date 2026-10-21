package com.axiom.calculator.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCalculationDao {
    @Insert
    suspend fun insert(entity: SavedCalculationEntity): Long

    @Update
    suspend fun update(entity: SavedCalculationEntity)

    @Query("SELECT * FROM saved_calculations ORDER BY isPinned DESC, updatedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SavedCalculationEntity>>

    @Query("SELECT * FROM saved_calculations WHERE isFavorite = 1 ORDER BY updatedAtEpochMillis DESC")
    fun observeFavorites(): Flow<List<SavedCalculationEntity>>

    @Query("SELECT * FROM saved_calculations WHERE label LIKE '%' || :query || '%' OR input LIKE '%' || :query || '%' ORDER BY updatedAtEpochMillis DESC")
    fun search(query: String): Flow<List<SavedCalculationEntity>>

    @Query("DELETE FROM saved_calculations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_calculations WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
