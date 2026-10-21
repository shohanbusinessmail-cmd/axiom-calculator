package com.axiom.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axiom.calculator.data.datastore.ThemePreference
import com.axiom.calculator.navigation.AxiomNavHost
import com.axiom.calculator.ui.theme.AxiomCalculatorTheme
import com.axiom.calculator.ui.theme.AxiomThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as AxiomApplication).container

        setContent {
            val preferences by container.settingsRepository.preferences.collectAsStateWithLifecycle(
                initialValue = com.axiom.calculator.data.datastore.AxiomPreferences()
            )
            val themeMode = when (preferences.theme) {
                ThemePreference.SYSTEM -> AxiomThemeMode.SYSTEM
                ThemePreference.LIGHT -> AxiomThemeMode.LIGHT
                ThemePreference.DARK -> AxiomThemeMode.DARK
            }
            AxiomCalculatorTheme(themeMode = themeMode) {
                AxiomNavHost(container)
            }
        }
    }
}
