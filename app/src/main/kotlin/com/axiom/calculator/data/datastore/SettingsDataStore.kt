package com.axiom.calculator.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "axiom_settings")

enum class ThemePreference { SYSTEM, LIGHT, DARK }
enum class AngleModePreference { DEGREES, RADIANS }
enum class NumberGroupingStyle { WESTERN, INDIAN, NONE }

data class AxiomPreferences(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val angleMode: AngleModePreference = AngleModePreference.DEGREES,
    val displayPrecision: Int = 10,
    val useScientificNotation: Boolean = true,
    val useEngineeringNotation: Boolean = false,
    val numberGrouping: NumberGroupingStyle = NumberGroupingStyle.WESTERN,
    val hapticFeedbackEnabled: Boolean = true,
    /** Key sound is OFF by default per SECTION 13. */
    val keySoundEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val historyRetentionKey: String = "30d"
)

/** Thin DataStore wrapper; [SettingsRepository] is the architectural boundary the rest of the app depends on. */
class SettingsDataStore(private val context: Context) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val ANGLE_MODE = stringPreferencesKey("angle_mode")
        val PRECISION = intPreferencesKey("precision")
        val SCIENTIFIC_NOTATION = booleanPreferencesKey("scientific_notation")
        val ENGINEERING_NOTATION = booleanPreferencesKey("engineering_notation")
        val GROUPING = stringPreferencesKey("number_grouping")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val KEY_SOUND = booleanPreferencesKey("key_sound_enabled")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val RETENTION = stringPreferencesKey("history_retention")
        val MEMORY_VALUE = stringPreferencesKey("calculator_memory_value")
    }

    /**
     * Calculator memory (MC/MR/M+/M-/MS) persistence policy: the stored value
     * survives process death and device restarts (it lives in DataStore, not
     * in-memory ViewModel state), and is cleared only by an explicit MC action.
     */
    val memoryValueFlow: Flow<String?> = context.dataStore.data.map { it[Keys.MEMORY_VALUE] }

    suspend fun setMemoryValue(value: String?) = context.dataStore.edit {
        if (value == null) it.remove(Keys.MEMORY_VALUE) else it[Keys.MEMORY_VALUE] = value
    }

    val preferencesFlow: Flow<AxiomPreferences> = context.dataStore.data.map { prefs ->
        AxiomPreferences(
            theme = prefs[Keys.THEME]?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() } ?: ThemePreference.SYSTEM,
            angleMode = prefs[Keys.ANGLE_MODE]?.let { runCatching { AngleModePreference.valueOf(it) }.getOrNull() } ?: AngleModePreference.DEGREES,
            displayPrecision = prefs[Keys.PRECISION] ?: 10,
            useScientificNotation = prefs[Keys.SCIENTIFIC_NOTATION] ?: true,
            useEngineeringNotation = prefs[Keys.ENGINEERING_NOTATION] ?: false,
            numberGrouping = prefs[Keys.GROUPING]?.let { runCatching { NumberGroupingStyle.valueOf(it) }.getOrNull() } ?: NumberGroupingStyle.WESTERN,
            hapticFeedbackEnabled = prefs[Keys.HAPTICS] ?: true,
            keySoundEnabled = prefs[Keys.KEY_SOUND] ?: false,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            historyRetentionKey = prefs[Keys.RETENTION] ?: "30d"
        )
    }

    suspend fun setTheme(theme: ThemePreference) = context.dataStore.edit { it[Keys.THEME] = theme.name }
    suspend fun setAngleMode(mode: AngleModePreference) = context.dataStore.edit { it[Keys.ANGLE_MODE] = mode.name }
    suspend fun setPrecision(precision: Int) = context.dataStore.edit { it[Keys.PRECISION] = precision }
    suspend fun setScientificNotation(enabled: Boolean) = context.dataStore.edit { it[Keys.SCIENTIFIC_NOTATION] = enabled }
    suspend fun setEngineeringNotation(enabled: Boolean) = context.dataStore.edit { it[Keys.ENGINEERING_NOTATION] = enabled }
    suspend fun setNumberGrouping(style: NumberGroupingStyle) = context.dataStore.edit { it[Keys.GROUPING] = style.name }
    suspend fun setHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setKeySound(enabled: Boolean) = context.dataStore.edit { it[Keys.KEY_SOUND] = enabled }
    suspend fun setReducedMotion(enabled: Boolean) = context.dataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    suspend fun setRetention(key: String) = context.dataStore.edit { it[Keys.RETENTION] = key }
}
