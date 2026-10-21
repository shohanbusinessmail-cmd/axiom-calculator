package com.axiom.calculator.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.axiom.calculator.di.AppContainer
import com.axiom.calculator.di.AxiomViewModelFactory
import com.axiom.calculator.feature.about.AboutScreen
import com.axiom.calculator.feature.formulas.FormulaDetailScreen
import com.axiom.calculator.feature.formulas.FormulaDetailViewModel
import com.axiom.calculator.feature.formulas.FormulasScreen
import com.axiom.calculator.feature.formulas.FormulasViewModel
import com.axiom.calculator.feature.library.LibraryScreen
import com.axiom.calculator.feature.library.LibraryViewModel
import com.axiom.calculator.feature.quicksolve.QuickSolveScreen
import com.axiom.calculator.feature.quicksolve.QuickSolveViewModel
import com.axiom.calculator.feature.scientific.ScientificScreen
import com.axiom.calculator.feature.scientific.ScientificViewModel
import com.axiom.calculator.feature.settings.SettingsScreen
import com.axiom.calculator.feature.settings.SettingsViewModel
import com.axiom.calculator.feature.standard.StandardScreen
import com.axiom.calculator.feature.standard.StandardViewModel
import com.axiom.calculator.feature.tools.ToolsScreen
import com.axiom.calculator.feature.tools.ToolsViewModel

@Composable
fun AxiomNavHost(container: AppContainer) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AxiomBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AxiomDestination.Standard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AxiomDestination.Standard.route) {
                val vm: StandardViewModel = viewModel(factory = AxiomViewModelFactory {
                    StandardViewModel(container.historyRepository, container.settingsRepository.dataStoreDelegate)
                })
                StandardScreen(vm)
            }
            composable(AxiomDestination.Scientific.route) {
                val vm: ScientificViewModel = viewModel(factory = AxiomViewModelFactory { ScientificViewModel(container.historyRepository) })
                ScientificScreen(vm)
            }
            composable(AxiomDestination.Formulas.route) {
                val vm: FormulasViewModel = viewModel(factory = AxiomViewModelFactory { FormulasViewModel() })
                FormulasScreen(vm) { formulaId -> navController.navigate(SecondaryRoutes.formulaDetail(formulaId)) }
            }
            composable(SecondaryRoutes.FORMULA_DETAIL) { backStackEntry ->
                val formulaId = backStackEntry.arguments?.getString("formulaId") ?: ""
                val vm: FormulaDetailViewModel = viewModel(factory = AxiomViewModelFactory {
                    FormulaDetailViewModel(formulaId, container.historyRepository, container.savedCalculationRepository)
                })
                FormulaDetailScreen(vm) { navController.popBackStack() }
            }
            composable(AxiomDestination.QuickSolve.route) {
                val vm: QuickSolveViewModel = viewModel(factory = AxiomViewModelFactory { QuickSolveViewModel(container.historyRepository) })
                QuickSolveScreen(vm)
            }
            composable(AxiomDestination.Tools.route) {
                val vm: ToolsViewModel = viewModel(factory = AxiomViewModelFactory { ToolsViewModel(container.historyRepository) })
                ToolsScreen(vm)
            }
            composable(AxiomDestination.Library.route) {
                val vm: LibraryViewModel = viewModel(factory = AxiomViewModelFactory {
                    LibraryViewModel(container.historyRepository, container.savedCalculationRepository, container.settingsRepository)
                })
                LibraryScreen(vm, onOpenSettings = { navController.navigate(SecondaryRoutes.SETTINGS) })
            }
            composable(SecondaryRoutes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = AxiomViewModelFactory {
                    SettingsViewModel(container.settingsRepository, container.settingsRepository.dataStoreDelegate, container.historyRepository, container.savedCalculationRepository)
                })
                SettingsScreen(vm, onBack = { navController.popBackStack() }, onOpenAbout = { navController.navigate(SecondaryRoutes.ABOUT) })
            }
            composable(SecondaryRoutes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun AxiomBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    NavigationBar {
        AxiomDestination.bottomNavItems.forEach { destination ->
            val selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}
