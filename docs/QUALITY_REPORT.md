# Quality Report

This report consolidates every verification activity actually performed
against Axiom Calculator. It is deliberately conservative: a line only
appears as "verified" if a command was actually run and its output is
reproduced or paraphrased accurately. Anything not run is labeled "not
performed" rather than omitted.

## 1. Source inventory (actually counted, this run)

```
$ find app/src/main -name "*.kt" | wc -l
66
$ find app/src/test -name "*.kt" | wc -l
10
```

| Area | Files |
|---|---|
| `core/` (math, solve, formula, units, programmer, datetime, util) | 29 |
| `data/` (db, repository, datastore) | 10 |
| `di/` | 2 |
| `feature/` (standard, scientific, formulas, solve, tools, library, settings, about) | 17 |
| `navigation/` | 2 |
| `ui/` (theme) | 4 |
| App/Activity roots | 2 |
| **Main total** | **66** |
| Test sources | 10 |
| **Grand total** | **76** |

## 2. Compilation / build / lint / instrumentation status

**Not performed. Cannot be performed in this sandbox.** No JDK, Android SDK,
or Gradle distribution is installed, and the sandbox has no network route to
any host that could provide them (`dl.google.com`, `repo1.maven.org`,
`services.gradle.org`, `maven.google.com`, `objects.githubusercontent.com`
all fail to connect). Full diagnostic transcript and exact commands a
maintainer must run are in `docs/BUILD_AND_RELEASE.md`. This report does
**not** claim:
- Kotlin source compiles
- `./gradlew test` passes
- `./gradlew lint` is clean
- any APK/AAB was produced
- any instrumented/device/emulator test ran

## 3. Algorithmic verification (actually performed)

A standalone Python re-implementation of the core numeric algorithms
(`scripts/verification/engine_reference.py`, 26,687 bytes) was written to
mirror the Kotlin math engine's documented behavior, and exercised against
every mandatory SECTION 17 vector (`scripts/verification/run_vectors.py`).
This was actually executed in this sandbox (Python 3 is available).

```
$ python3 scripts/verification/run_vectors.py > scripts/verification/reference_vector_results.txt
$ cat scripts/verification/reference_vector_results.txt
PASSED: 113
FAILED: 0

All mandatory Section 17 reference vectors passed against the Python
algorithmic reference.
2026-10-21T19:53:17Z
```

Coverage: arithmetic & precedence, trig (deg/rad), logs, factorial/nPr/nCr,
complex arithmetic, percentage family, finance incl. EMI, geometry,
linear/quadratic/2x2-system equation solving, statistics, matrix ops, unit
conversion incl. temperature, programmer/bitwise incl. two's complement and
logical-vs-arithmetic shift, date arithmetic incl. leap years and
month-end clamping.

**What this does and does not prove:**
- It proves the *algorithms as designed* produce the spec's expected numeric
  outputs, for the specific vectors tested.
- It does **not** prove the Kotlin source (`core/math/*.kt`,
  `core/solve/*.kt`, etc.) compiles or behaves identically, since the Kotlin
  code was never compiled or executed. The Kotlin implementation was written
  to mirror the same algorithm, then the test files in `app/src/test`
  encode the same vectors as JUnit assertions for when a real toolchain is
  available — but those JUnit tests are themselves unexecuted (§4).

## 4. Kotlin unit test suite (written, NOT executed)

| File | Vectors / scope | Executed? |
|---|---|---|
| `core/math/ExpressionEngineTest.kt` | Parser/precedence/associativity edge cases | No |
| `core/math/StandardCalculatorEngineTest.kt` | Standard-tab arithmetic, %, memory, errors | No |
| `core/solve/EquationSolverTest.kt` | Linear/quadratic/2x2-system (E001-E009) | No |
| `core/solve/MatrixTest.kt` | Matrix add/mul/det/inverse/rank + dimension-mismatch errors | No |
| `core/solve/StatisticsTest.kt` | Mean/median/mode/variance/stddev/quartiles | No |
| `core/units/UnitConverterTest.kt` | U001-U011, temperature, round-trip, category mismatch | No |
| `core/programmer/ProgrammerToolsTest.kt` | B001-B007, two's complement, shift semantics | No |
| `core/datetime/DateMathTest.kt` | Leap years, month clamping, ISO parse errors | No |
| `core/formula/FormulaRegistryTest.kt` | Registry integrity + P/F/G formula vectors | No |
| `data/repository/HistoryRepositoryTest.kt` | Retention cleanup with injectable fake clock | No |

10 files, 76 total `.kt` sources. **Zero of these test files have ever been
run by a JVM.** They are provided so that the moment a maintainer has a
working Gradle/JDK/Android SDK setup, `./gradlew test` gives immediate,
real, automated confirmation — see `docs/BUILD_AND_RELEASE.md` for the exact
command. Until that command is run by someone with such an environment,
these files are evidence of *test design*, not test *results*.

## 5. Static code review (actually performed)

- A Python brace/parenthesis balance scan was run across all 76 `.kt` files.
  6 files showed a nonzero raw count; manual inspection attributed each to
  `Char` literal parentheses (e.g. `'(' -> TokenType.LPAREN`) rather than a
  true structural imbalance. This is a weak heuristic, not a parse-level
  guarantee, and does not substitute for a compiler.
- Repo-wide `grep` for forbidden networking/telemetry APIs
  (`HttpURLConnection`, `okhttp`, `java.net.Socket`, `WebView`, known
  analytics/crash-reporting package names) returned no matches under
  `app/src/main/kotlin`, see `docs/OFFLINE_AND_PRIVACY.md`.
- `AndroidManifest.xml` was read directly and contains zero
  `<uses-permission>` entries.
- `app/build.gradle.kts` dependency block was read directly; contains only
  AndroidX/Compose/Room/DataStore/Kotlin coroutines/JUnit — no ad SDK,
  analytics SDK, or network client library.

## 6. Manual code review spot-checks

- `EquationSolver`, `Matrix`, `Statistics`, `UnitConverter`,
  `ProgrammerTools`, `DateMath`, and the `FormulaRegistry` formula
  definitions were read in full during this and prior sessions and compared
  line-by-line against the Python reference's logic structure (not against a
  compiler) to reduce the chance of a transcription error between the two
  implementations.
- **Update (final pass):** every test file written in this session
  (`UnitConverterTest`, `ProgrammerToolsTest`, `DateMathTest`,
  `FormulaRegistryTest`, `HistoryRepositoryTest`) was re-diffed, function
  signature by function signature, against the actual source it tests
  (`UnitConverter.kt`, `ProgrammerTools.kt`, `DateMath.kt`,
  `FormulaRegistry.kt` + `PercentageFormulas.kt`/`FinanceFormulas.kt`/
  `GeometryFormulas.kt`, `HistoryDao.kt`/`HistoryRepository.kt`/
  `RetentionPolicy.kt`/`AxClock.kt`/`HistoryEntity.kt`). All assumed method
  names, parameter types, property names, default values, and thrown
  exception types were confirmed to match the real source exactly (read
  directly in this session; not inferred). This does **not** mean the tests
  pass — only a real compiler/JVM run can prove that — but it removes the
  "unverified API signature" risk that earlier notes in this project's
  working history flagged as the single highest-risk open item. This
  read-through did not extend to the earlier-session test files
  (`ExpressionEngineTest`, `StandardCalculatorEngineTest`,
  `EquationSolverTest`, `MatrixTest`, `StatisticsTest`), which remain in the
  weaker "written, not re-verified this pass" category.

## 7. Overall quality verdict

| Dimension | Verdict |
|---|---|
| Feature completeness vs. spec | Mostly complete; gaps enumerated exhaustively in `docs/KNOWN_LIMITATIONS.md` |
| Mathematical correctness | Strong evidence (113/113 executed Python vectors) for the algorithms; no compiler-level evidence for the Kotlin port |
| Offline/privacy compliance | Strong static evidence; no runtime/device evidence |
| Build health | Unknown — never compiled |
| Test health | Unknown — tests written, never executed |
| Release readiness | Not release-ready: unsigned, unbuilt, untested by any JVM |

This report should be read together with `docs/BUILD_AND_RELEASE.md`
(the blocker and exact reproduction commands), `docs/TEST_STRATEGY.md` (the
two-layer verification model), and `docs/KNOWN_LIMITATIONS.md` (every scope
gap).
