# Architecture

## Technology stack (actually used)

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3, custom theme (`ui/theme`) |
| Architecture | MVVM, unidirectional data flow (`StateFlow` UI state, explicit event functions) |
| Async | Kotlin Coroutines + Flow |
| Local database | Room (`data/db`) |
| Preferences | DataStore Preferences (`data/datastore`) |
| Build | Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`) |
| DI | Manual (`di/AppContainer.kt`, `di/AxiomViewModelFactory.kt`) — no DI framework, since the dependency graph is small and a framework (Hilt/Koin) would add build complexity without a clear benefit here |
| Unit tests | JUnit4 (+ a hand-written in-memory fake DAO for repository tests) |

## Module/package boundaries

```
core/math       Tokenizer, parser, AST, Double evaluator, BigDecimal evaluator,
                number formatting, complex numbers, fractions, typed errors.
                Pure Kotlin, zero Android imports, unit-testable on the plain JVM.
core/solve      Equation solvers (linear/quadratic/2x2/3x3), number theory,
                sequences, statistics, matrices. Pure Kotlin.
core/formula    Formula interface + registry + category implementations
                (percentage, ratio, finance, everyday, geometry, physics).
                Pure Kotlin.
core/units      Unit conversion tables + temperature affine conversion. Pure Kotlin.
core/programmer Base conversion, two's-complement bitwise operations. Pure Kotlin.
core/datetime   java.time-based date arithmetic, no implicit "now". Pure Kotlin.
core/util       Injectable AxClock (SystemAxClock / FixedAxClock for tests).

data/db         Room entities + DAOs (HistoryEntity/Dao, SavedCalculationEntity/Dao).
data/repository Repositories: HistoryRepository, SavedCalculationRepository,
                SettingsRepository. ViewModels depend ONLY on these, never on
                DAOs or DataStore directly.
data/datastore  DataStore-backed SettingsDataStore (preferences schema + edits).

di              Manual dependency container + generic ViewModel factory.
navigation      Destinations + AxiomNavHost (NavController wiring, bottom nav).
ui/theme        Color/Type/Shape/Theme — centralized Material 3 design tokens.

feature/<tab>   One package per tab (standard, scientific, formulas, quicksolve,
                tools, library, settings, about): a ViewModel (state + events,
                calls into core/* and data/repository/*) and a Composable screen
                (renders state, forwards user actions — contains NO calculation
                logic and NO direct database access).
```

## Enforced boundaries

- **UI composables contain no calculation algorithms.** Every screen reads a
  `StateFlow<UiState>` and calls ViewModel functions; all math happens in
  `core/*`.
- **ViewModels never run database queries directly.** They call
  `HistoryRepository`/`SavedCalculationRepository`/`SettingsRepository`,
  which wrap the Room DAOs / DataStore.
- **Repositories do not depend on UI.** They take a DAO/DataStore and an
  `AxClock` in their constructor and expose `Flow`s and suspend functions.
- **Mathematical functions are UI-free and directly unit-testable** — see
  every file under `core/math`, `core/solve`, `core/formula`, `core/units`,
  `core/programmer`, `core/datetime`.
- **History commits are explicit.** A `HistoryRepository.record(...)` call
  happens only from an explicit user action (pressing "=", a formula's
  "Calculate" button, a Quick Solve "Solve" button, a Tools "Calculate"
  button) — never from Compose recomposition.
- **Favorites/saved items are structurally independent of history** (separate
  Room table), so automatic history retention cleanup can never delete them.

## Why six tabs are structurally separate screens, not one scrolling screen

Each of Standard, Scientific, Formulas, Quick Solve, Tools, and Library is a
distinct navigation destination (`navigation/Destinations.kt`) with its own
ViewModel instance, backed by Jetpack Navigation Compose
(`navigation/AxiomNavHost.kt`) with a bottom navigation bar. Nested screens
(formula detail, settings, about) are pushed on top of the nav stack rather
than being modal dialogs glued onto a single screen.
