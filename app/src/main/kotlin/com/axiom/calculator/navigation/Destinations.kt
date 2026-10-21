package com.axiom.calculator.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.ui.graphics.vector.ImageVector

/** The exact six primary destinations required by SECTION 5. */
sealed class AxiomDestination(val route: String, val label: String, val icon: ImageVector) {
    object Standard : AxiomDestination("standard", "Standard", Icons.Filled.Calculate)
    object Scientific : AxiomDestination("scientific", "Scientific", Icons.Filled.Science)
    object Formulas : AxiomDestination("formulas", "Formulas", Icons.Filled.Functions)
    object QuickSolve : AxiomDestination("quick_solve", "Quick Solve", Icons.Filled.Bolt)
    object Tools : AxiomDestination("tools", "Tools", Icons.Filled.Build)
    object Library : AxiomDestination("library", "Library", Icons.Filled.LocalLibrary)

    companion object {
        val bottomNavItems = listOf(Standard, Scientific, Formulas, QuickSolve, Tools, Library)
    }
}

/** Secondary (non-bottom-nav) routes, reached by in-screen navigation actions. */
object SecondaryRoutes {
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val FORMULA_DETAIL = "formula_detail/{formulaId}"
    fun formulaDetail(id: String) = "formula_detail/$id"
    const val SAVED = "saved"
    const val REFERENCE = "reference"
}
