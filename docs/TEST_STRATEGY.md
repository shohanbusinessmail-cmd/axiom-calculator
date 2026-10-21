# Test Strategy

## Two layers of verification in this repository

1. **`scripts/verification/` (Python, actually executed in this sandbox).**
   `engine_reference.py` is a structural reference implementation of the
   tokenizer/parser/evaluator/solvers/formulas. `run_vectors.py` runs every
   mandatory SECTION 17 vector against it. This was run and produced
   **113/113 passed** — see `docs/QUALITY_REPORT.md` and
   `scripts/verification/reference_vector_results.txt`. This is real,
   reproducible evidence of the *algorithm's* correctness, obtained because
   this sandbox has Python but no JDK/Android SDK/Gradle (see
   `docs/BUILD_AND_RELEASE.md`).

2. **`app/src/test/kotlin/...` (JUnit4, written but NOT executed).** Every
   Kotlin production module has a corresponding test file that is a direct
   structural port of the already-verified Python/mandatory vectors, plus
   additional edge cases (empty datasets, singular matrices, invalid bases,
   leap years, etc.). These tests **could not be compiled or run** in this
   sandbox. They are real, complete test source files — not placeholders —
   and must be run with `./gradlew test` by a maintainer with a working
   toolchain. Reporting them as "passing" without that run would violate the
   evidence-based engineering requirement, so this document and the final
   report explicitly label them "written, not executed."

## Test inventory (by file)

| File | Covers |
|---|---|
| `core/math/ExpressionEngineTest.kt` | M001–M017, T001–T009, S001–S010, C001–C005 (SECTION 17.1–17.4) |
| `core/math/StandardCalculatorEngineTest.kt` | M005 (BigDecimal 0.1+0.2), expression-editing rules, division-by-zero/unsupported-function typed errors |
| `core/solve/EquationSolverTest.kt` | E001–E009 (SECTION 17.8), equation-string-parser degree rejection |
| `core/solve/StatisticsTest.kt` | SECTION 17.9 dataset, single-value dataset, multiple/no-mode cases, empty dataset |
| `core/solve/MatrixTest.kt` | SECTION 17.10 matrix vectors, singular matrix, identity, incompatible dimensions |
| `core/units/UnitConverterTest.kt` | U001–U011 (SECTION 17.11), round-trip conversion, incompatible-category rejection |
| `core/programmer/ProgrammerToolsTest.kt` | B001–B008 (SECTION 17.12), explicit-bit-width NOT, base round-trip, logical vs. arithmetic shift |
| `core/datetime/DateMathTest.kt` | Leap years (incl. century rule), end-of-month clamping, year transition, negative duration, invalid date |
| `core/formula/FormulaRegistryTest.kt` | P001–P009, F001–F006, G001/G004–G008 (SECTION 17.5–17.7), registry ID uniqueness, category coverage |
| `data/repository/HistoryRepositoryTest.kt` | Retention cleanup with an injectable `FixedAxClock` (no sleep-based timing), `NEVER` policy semantics, immediate read-after-write |

## Explicitly avoided anti-patterns (per SECTION 18)

- No sleep-based expiration tests — `HistoryRepositoryTest` uses
  `FixedAxClock.advanceBy(...)` instead of real time.
- No network-dependent tests.
- No disabled/skipped tests hiding failures.
- No weakened assertions to force a green build — every assertion uses the
  exact expected value (or a documented numerical tolerance, e.g. `1e-9`/
  `1e-10` for transcendental functions) from the specification.

## What is NOT covered (explicitly, not silently)

- Compose UI tests (`androidx.compose.ui.test`) and instrumented
  (`androidTest`) tests: the Gradle `androidTest` dependencies are declared
  and the environment supports adding such tests, but none were written in
  this pass, and none could run without a device/emulator (unavailable here).
  See `docs/KNOWN_LIMITATIONS.md`.
- Property-based testing libraries (e.g. Kotest property testing) are not
  included in the dependency set; the "property-style" invariants the spec
  asks for (round-trip unit conversion, round-trip number-base conversion,
  identity-matrix multiplication) are instead covered by targeted
  example-based tests that exercise the same invariant.
