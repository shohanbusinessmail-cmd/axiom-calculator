package com.axiom.calculator.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-saved calculation (LIB favorites/pinned). Deliberately stored in a
 * table separate from [HistoryEntity] so automatic history retention cleanup
 * can never delete a saved/favorited/pinned item (SECTION 12).
 */
@Entity(tableName = "saved_calculations")
data class SavedCalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val input: String,
    val result: String,
    val calculationType: String,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
