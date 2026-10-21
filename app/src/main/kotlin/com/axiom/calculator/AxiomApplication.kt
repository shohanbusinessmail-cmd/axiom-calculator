package com.axiom.calculator

import android.app.Application
import com.axiom.calculator.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Application entry point. No analytics, crash reporting, or remote
 * configuration SDKs are initialized here or anywhere else in the app —
 * see docs/OFFLINE_AND_PRIVACY.md.
 */
class AxiomApplication : Application() {
    val container by lazy { AppContainer(this) }

    /** Process-lifetime scope used only for the startup retention cleanup sweep. */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Best-effort cleanup at process start, in addition to cleanup performed
        // when the Library screen is opened. Android may not run a background
        // scheduler reliably, so this must not be the only cleanup trigger
        // (SECTION 12).
        applicationScope.launch {
            runCatching {
                val current = container.settingsRepository.retentionPolicy.first()
                container.historyRepository.cleanupExpired(current)
            }
        }
    }
}
