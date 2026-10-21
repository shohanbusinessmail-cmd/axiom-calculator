package com.axiom.calculator.di

import android.content.Context
import com.axiom.calculator.core.util.AxClock
import com.axiom.calculator.core.util.SystemAxClock
import com.axiom.calculator.data.datastore.SettingsDataStore
import com.axiom.calculator.data.db.AxiomDatabase
import com.axiom.calculator.data.repository.HistoryRepository
import com.axiom.calculator.data.repository.SavedCalculationRepository
import com.axiom.calculator.data.repository.SettingsRepository

/**
 * Manual, hand-written dependency container (SECTION 3: "use manual
 * dependency injection initially unless a framework offers a clear,
 * justified benefit"). A single instance is created in [AxiomApplication]
 * and handed to each feature's ViewModel factory.
 */
class AppContainer(context: Context, clock: AxClock = SystemAxClock) {
    private val appContext = context.applicationContext

    val clock: AxClock = clock

    private val database by lazy { AxiomDatabase.getInstance(appContext) }

    val historyRepository: HistoryRepository by lazy { HistoryRepository(database.historyDao(), clock) }
    val savedCalculationRepository: SavedCalculationRepository by lazy { SavedCalculationRepository(database.savedCalculationDao(), clock) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(SettingsDataStore(appContext)) }
}
