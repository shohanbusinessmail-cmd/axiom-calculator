package com.axiom.calculator.data.repository

import com.axiom.calculator.data.datastore.AxiomPreferences
import com.axiom.calculator.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Architectural boundary ViewModels depend on; wraps the DataStore-backed [SettingsDataStore]. */
class SettingsRepository(private val dataStore: SettingsDataStore) {
    val preferences: Flow<AxiomPreferences> = dataStore.preferencesFlow

    val retentionPolicy: Flow<RetentionPolicy> = preferences.map { RetentionPolicy.fromStorageKey(it.historyRetentionKey) }

    suspend fun setRetentionPolicy(policy: RetentionPolicy) = dataStore.setRetention(policy.storageKey)

    val dataStoreDelegate: SettingsDataStore get() = dataStore
}
