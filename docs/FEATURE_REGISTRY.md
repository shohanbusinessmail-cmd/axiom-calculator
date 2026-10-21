# Feature Registry

Status legend used below (see SECTION 4 of the spec for the full list):
- **IMPLEMENTED** — source code exists and is believed correct by design/manual review.
- **ALGO-VERIFIED** — IMPLEMENTED, and additionally the algorithm was independently
  verified correct via the executed Python reference harness
  (`scripts/verification/`), because the Kotlin port could not be compiled/run
  in this sandbox (see `docs/BUILD_AND_RELEASE.md`). This is the strongest
  claim this repository makes for any single feature.
- **TEST-WRITTEN** — a corresponding JUnit test file exists under
  `app/src/test/kotlin` but has never been executed (no JDK/Gradle in this
  sandbox).
- **BUILD_VERIFIED / DEVICE_VERIFIED / UI_TESTED** — **not used anywhere in
  this document**, because no build, device, or UI test was ever actually
  executed. Using these labels without that evidence would violate the
  evidence-based engineering requirement.
- **BLOCKED** — cannot be verified further in this environment.
- **INCOMPLETE** — deliberately out of scope for this pass; see
  `docs/KNOWN_LIMITATIONS.md`.

## Standard Calculator (STD)

| ID | Behavior | Location | Status |
|---|---|---|---|
| STD-001 | Add/subtract/multiply/divide, parentheses, decimals | `core/math/ExpressionParser.kt`, `BigDecimalEvaluator.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| STD-002 | Operator precedence, associativity, unary minus, `2^3^2`/`-2^2`/`(-2)^2` conventions | `core/math/ExpressionParser.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| STD-003 | Contextual percent (`+`/`-` vs `*`/`/`) | `core/math/DecimalArithmetic.kt` | IMPLEMENTED |
| STD-004 | Positive/negative toggle, clear entry, clear all, backspace | `core/math/StandardCalculatorEngine.kt`, `StandardViewModel.kt` | IMPLEMENTED, TEST-WRITTEN |
| STD-005 | Undo/redo | `StandardViewModel.kt` (bounded 50-entry stack) | IMPLEMENTED |
| STD-006 | Memory MC/MR/M+/M-/MS with persistence across process restarts | `SettingsDataStore.memoryValueFlow`, `StandardViewModel.kt` | IMPLEMENTED |
| STD-007 | Division by zero / empty / malformed expression → typed error, no crash | `core/math/MathError.kt` + `StandardCalculatorEngineTest.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| STD-008 | History commit on "=" only | `StandardViewModel.onEquals()` | IMPLEMENTED |
| STD-009 | Copy expression/result, paste | Not implemented (requires Android `ClipboardManager` integration) | INCOMPLETE — see Known Limitations |

## Scientific Calculator (SCI)

| ID | Behavior | Location | Status |
|---|---|---|---|
| SCI-001 | Trig (sin/cos/tan/asin/acos/atan/atan2) + hyperbolic family | `core/math/Evaluator.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| SCI-002 | DEG/RAD persistent mode, displayed prominently | `ScientificViewModel.kt`, `ScientificScreen.kt` | IMPLEMENTED (DEG/RAD toggle is in-session; see Known Limitations for persistence across restarts) |
| SCI-003 | ln/log10/log2/log(base)/exp, domain errors | `core/math/Evaluator.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| SCI-004 | Factorial, nPr, nCr (exact `BigInteger`) | `core/math/Evaluator.kt`, `MathFunctions.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| SCI-005 | sqrt/cbrt/nth root, abs/floor/ceil/round/trunc, sign | `core/math/Evaluator.kt` | ALGO-VERIFIED (sqrt/abs), IMPLEMENTED (rest) |
| SCI-006 | Complex numbers: +,-,*,/, abs, arg, conjugate | `core/math/Complex.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| SCI-006b | Complex powers/roots | `Complex.pow()` | IMPLEMENTED (not covered by a mandatory vector; manually reviewed only) |
| SCI-007 | Fractions: simplify, decimal↔fraction | `core/math/Fraction.kt` | IMPLEMENTED |
| SCI-008 | Matrix arithmetic, transpose, determinant, inverse, rank | `core/solve/Matrix.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| SCI-009 | Scientific/engineering notation, configurable precision | `core/math/NumberFormatter.kt` | IMPLEMENTED |
| SCI-010 | Dedicated complex-number editor screen | Not implemented as a separate UI; complex arithmetic exists only in the engine/tests | INCOMPLETE — see Known Limitations |
| SCI-011 | Polynomial root finding beyond degree 2 | Not implemented | INCOMPLETE — see Known Limitations |

## Formulas (FOR)

All formulas are registered in `core/formula/FormulaRegistry.kt` with a
stable ID, searchable via the Formulas tab. See that file and
`docs/MATHEMATICAL_SEMANTICS.md` §5 for the full catalog. Representative,
mandatory-vector-covered subset:

| ID | Title | Status |
|---|---|---|
| FOR-PCT-001..009 | Percentage family (X% of Y, reverse %, discount, markup, margin, ...) | ALGO-VERIFIED (P001–P009), TEST-WRITTEN |
| FOR-FIN-001..005 | Simple/compound interest, EMI, profit margin, markup % | ALGO-VERIFIED (F001–F006), TEST-WRITTEN |
| FOR-GEO-001..013 | Circle, rectangle, square, triangle, trapezoid, cube, cuboid, sphere, hemisphere, cylinder, cone, annulus, parallelogram | ALGO-VERIFIED (G001–G008 subset), TEST-WRITTEN |
| FOR-PHY-001..009 | Density, force, work, power, Ohm's law, electrical power, kinetic/potential energy, momentum | IMPLEMENTED (not individually in SECTION 17 mandatory vectors; formulas are standard textbook identities, manually reviewed) |
| FOR-RATIO-001..004 | Simplify/scale ratio, solve proportion, split by ratio | IMPLEMENTED |
| FOR-EVY-001..004 | Speed, unit price, weighted average, combined work rate | IMPLEMENTED |

Geometry shapes explicitly **not yet implemented**: full pyramid family,
general prism — see `docs/KNOWN_LIMITATIONS.md`.

## Quick Solve (SOL)

| ID | Behavior | Location | Status |
|---|---|---|---|
| SOL-001 | Linear equation (`ax+b=0` and canonical string form `"2x+3=11"`) | `core/solve/EquationSolver.kt`, `SingleVariableEquationParser.kt` | ALGO-VERIFIED (E001, E006, E007), TEST-WRITTEN |
| SOL-002 | Quadratic — real and complex roots | `EquationSolver.kt` | ALGO-VERIFIED (E002–E005), TEST-WRITTEN |
| SOL-003 | 2-variable linear system (Cramer's rule) | `EquationSolver.kt` | ALGO-VERIFIED (E008, E009), TEST-WRITTEN |
| SOL-004 | 3-variable linear system (Gaussian elimination) | `EquationSolver.solveSystem3x3` | IMPLEMENTED (no dedicated UI form in Quick Solve screen; engine-level only — see Known Limitations) |
| SOL-005 | GCD/LCM/prime factorization/primality/divisors | `core/solve/NumberTheory.kt` | IMPLEMENTED |
| SOL-006 | Arithmetic/geometric sequences (nth term, sum) | `core/solve/Sequences.kt` | IMPLEMENTED |
| SOL-007 | Statistics (count/sum/mean/median/mode/variance/stddev/quartiles) | `core/solve/Statistics.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| SOL-008 | Matrix add/sub/mul/scalar/transpose/det/inverse/rank | `core/solve/Matrix.kt` | ALGO-VERIFIED, TEST-WRITTEN |
| SOL-009 | Polynomial evaluation/roots beyond degree 2, generic numerical root finding | Not implemented | INCOMPLETE — see Known Limitations |

## Tools (TOOL)

| ID | Behavior | Location | Status |
|---|---|---|---|
| TOOL-001 | Length/Area/Volume/Mass/Time/Speed/Pressure/Energy/Power conversion tables | `core/units/UnitConverter.kt` | ALGO-VERIFIED (U001–U011), TEST-WRITTEN |
| TOOL-002 | Temperature conversion (affine, not multiplicative) | `UnitConverter.convertTemperature` | ALGO-VERIFIED, TEST-WRITTEN |
| TOOL-003 | Base conversion (bin/oct/dec/hex), exact BigInteger | `core/programmer/ProgrammerTools.kt` | ALGO-VERIFIED (B001–B003), TEST-WRITTEN |
| TOOL-004 | Bitwise AND/OR/XOR/NOT, explicit bit width, two's complement | `ProgrammerTools.kt` | ALGO-VERIFIED (B004–B007), TEST-WRITTEN |
| TOOL-005 | Logical vs. arithmetic right shift | `ProgrammerTools.kt` | IMPLEMENTED, TEST-WRITTEN |
| TOOL-006 | Date difference, add/subtract days/months (end-of-month clamp), leap year, days-in-month | `core/datetime/DateMath.kt` | IMPLEMENTED, TEST-WRITTEN |
| TOOL-007 | BMI + BMR (Mifflin-St Jeor), explicitly labeled informational | `ToolsViewModel.runBmiBmr` | IMPLEMENTED |
| TOOL-008 | SI prefixes pico..tera reference | Not implemented as a standalone converter (covered partially by scientific/engineering notation) | INCOMPLETE — see Known Limitations |
| TOOL-009 | Significant-figure / absolute-relative-percentage error tools | Not implemented | INCOMPLETE — see Known Limitations |

## Library (LIB)

| ID | Behavior | Location | Status |
|---|---|---|---|
| LIB-001 | Persistent calculation history (Room), newest-first, search, per-type filter | `data/db/HistoryEntity.kt`/`HistoryDao.kt`, `LibraryViewModel.kt` | IMPLEMENTED, TEST-WRITTEN (repository layer) |
| LIB-002 | Automatic retention (1/7/30/90 days/Never, default 30), injectable-clock cleanup rule | `data/repository/HistoryRepository.kt`, `RetentionPolicy.kt` | ALGO-VERIFIED (repository test with `FixedAxClock`), TEST-WRITTEN |
| LIB-003 | Individual + multi-select + clear-all deletion, confirmation dialog for clear-all | `LibraryViewModel.kt`, `LibraryScreen.kt` | IMPLEMENTED |
| LIB-004 | Saved calculations, separate table, immune to history expiry | `data/db/SavedCalculationEntity.kt` | IMPLEMENTED |
| LIB-005 | Favorite/pin/rename/delete saved calculations | `SavedCalculationRepository.kt`, `LibraryScreen.kt` | IMPLEMENTED |
| LIB-006 | Formula/constants reference, searchable | `LibraryScreen.ReferenceList`, backed by `FormulaRegistry` | IMPLEMENTED (basic list; see Known Limitations for full trig-identity/SI-prefix reference breadth) |
| LIB-007 | Edit-and-recalculate a history entry | Not implemented (entries can be viewed/copied/deleted/saved, not re-opened into an editable calculator state) | INCOMPLETE — see Known Limitations |
| LIB-008 | Undo for destructive deletion | Not implemented | INCOMPLETE — see Known Limitations |

## Settings / Privacy (SET / PRIV)

| ID | Behavior | Location | Status |
|---|---|---|---|
| SET-001 | Theme: system/light/dark, centralized tokens | `ui/theme/*`, `SettingsViewModel.kt` | IMPLEMENTED |
| SET-002 | Angle mode, precision, notation, grouping preferences | `SettingsDataStore.kt`, `SettingsScreen.kt` | IMPLEMENTED |
| SET-003 | Haptics / key-sound (default OFF) / reduced-motion toggles | `SettingsDataStore.kt` | IMPLEMENTED (toggles are persisted; haptic/sound playback wiring into the keypad is NOT yet implemented — see Known Limitations) |
| SET-004 | Retention control + "run cleanup now" + separate clear history/clear saved | `SettingsScreen.kt`, `SettingsViewModel.kt` | IMPLEMENTED |
| SET-005 | No decorative/non-functional export button | Verified absent — no export UI exists anywhere in the app | IMPLEMENTED (by omission, per spec: "do not create a decorative export button without working export logic") |
| PRIV-001 | Offline runtime verification | `docs/OFFLINE_AND_PRIVACY.md` | BLOCKED (static inspection only done; no device available) |
| PRIV-002 | No INTERNET permission, no telemetry dependency | `AndroidManifest.xml`, `build.gradle.kts` | IMPLEMENTED, statically verified |

## About

| ID | Behavior | Location | Status |
|---|---|---|---|
| ABOUT-001 | App name, developer, email, version, privacy summary | `feature/about/AboutScreen.kt`, `strings.xml`, `BuildConfig` | IMPLEMENTED |
| ABOUT-002 | Working email intent with graceful no-email-app fallback | `AboutScreen.kt` | IMPLEMENTED |
