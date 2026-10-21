package com.axiom.calculator.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One committed calculation (LIB-001). A row is written only when a
 * calculation is explicitly "committed" — i.e. the user presses "=" (or a
 * formula/solver produces a final result) — never on composable
 * recomposition or intermediate keystrokes (SECTION 12/19).
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val input: String,
    val result: String,
    val calculationType: String, // e.g. "STANDARD", "SCIENTIFIC", "FORMULA:<id>", "SOLVE:<kind>"
    val angleMode: String? = null,
    val unitContext: String? = null,
    val precisionMetadata: String? = null,
    val createdAtEpochMillis: Long
)
