package com.axiom.calculator.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class, SavedCalculationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AxiomDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun savedCalculationDao(): SavedCalculationDao

    companion object {
        private const val DATABASE_NAME = "axiom-calculator.db"

        @Volatile
        private var instance: AxiomDatabase? = null

        fun getInstance(context: Context): AxiomDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AxiomDatabase::class.java,
                    DATABASE_NAME
                )
                    // No destructive fallback: a genuine schema change requires a
                    // written Migration so user history/saved items are never
                    // silently dropped (SECTION 19).
                    .build()
                    .also { instance = it }
            }
    }
}
