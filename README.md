# Axiom Calculator

A native Android calculator app — Kotlin, Jetpack Compose, Material 3, MVVM,
Room, DataStore. Fully offline: no internet permission, no ads, no
telemetry/analytics, no accounts, no paid APIs or subscriptions.

> **Build status: not yet compiled.** This repository was developed in a
> sandboxed environment with no JDK, Android SDK, or Gradle available (see
> `docs/BUILD_AND_RELEASE.md`). The source is complete and has been reviewed
> and, for the math engine, independently algorithm-verified via a Python
> reference harness (113/113 vectors — see `docs/QUALITY_REPORT.md`), but it
> has **never been compiled, unit-tested by a JVM, linted, or run on a
> device/emulator.** Read `docs/KNOWN_LIMITATIONS.md` and
> `docs/QUALITY_REPORT.md` before treating any part of this as
> production-verified.

## What it does

Six bottom-navigation tabs:

| Tab | Purpose |
|---|---|
| **Standard** | Everyday arithmetic calculator with memory, history, contextual percent |
| **Scientific** | Trig/log/power/root/factorial/combinatorics, complex numbers, fractions, matrices |
| **Formulas** | Catalog of percentage, ratio, finance (incl. EMI), everyday, geometry, and physics formulas |
| **Quick Solve** | Linear/quadratic equations, 2-variable systems, number theory, sequences, statistics, matrices |
| **Tools** | Unit conversion, programmer calculator (bases/bitwise), date math, BMI/BMR |
| **Library** | Room-backed calculation history with configurable retention, saved/favorite calculations, formula reference |

Plus a Settings screen (theme, angle mode, precision/notation, haptics,
retention policy) and an About screen.

## Tech stack

Kotlin, Jetpack Compose, Material 3, MVVM (`ViewModel` + `StateFlow`), Room
(history/saved persistence), DataStore (preferences), manual DI via
`AppContainer` (no Hilt/Dagger), Kotlin Coroutines. See
`docs/ARCHITECTURE.md` for the full package-boundary diagram and rationale.

## Repository layout

```
app/src/main/kotlin/com/axiom/calculator/
  core/        math/solve/formula/units/programmer/datetime engines (pure Kotlin, no Android deps)
  data/        Room entities/DAOs, repositories, DataStore preferences
  di/          manual dependency container
  feature/     one package per tab (standard, scientific, formulas, solve, tools, library, settings, about)
  navigation/  NavHost + bottom nav
  ui/theme/    Material 3 theme tokens
app/src/test/kotlin/...   JUnit test sources (written, not yet executed — see below)
docs/                     architecture, math semantics, privacy, build, test strategy, limitations, quality report
scripts/verification/     standalone Python reference implementation + Section 17 vector runner (executed; 113/113 pass)
```

## Why there's no APK here

No JDK, Android SDK, or Gradle distribution could be installed or reached
from the development sandbox (no network route to `dl.google.com`,
`repo1.maven.org`, `services.gradle.org`, or `maven.google.com`). This is
documented in full, with the exact commands to run once a real toolchain is
available, in `docs/BUILD_AND_RELEASE.md`.

## Building (for a maintainer with a real Android toolchain)

```bash
# Requires: JDK 17, Android SDK (compileSdk/targetSdk 34), network access.
./gradlew assembleDebug      # unsigned debug APK
./gradlew test               # run the JUnit suite under app/src/test
./gradlew lint               # Android lint
./gradlew assembleRelease    # unsigned release APK (sign manually — see docs/BUILD_AND_RELEASE.md)
```

If `gradle/wrapper/gradle-wrapper.jar` is missing in your checkout, regenerate
it with a locally installed Gradle: `gradle wrapper --gradle-version 8.9`.

## Documentation index

- `docs/ARCHITECTURE.md` — layers, package boundaries, enforced rules
- `docs/MATHEMATICAL_SEMANTICS.md` — grammar, ambiguity resolutions, numeric types per context, error model
- `docs/FEATURE_REGISTRY.md` — every mandated feature, its status, and its evidence level
- `docs/TEST_STRATEGY.md` — the two-layer (Python-executed / Kotlin-written) verification model
- `docs/OFFLINE_AND_PRIVACY.md` — what was statically verified about offline/privacy behavior
- `docs/BUILD_AND_RELEASE.md` — the build blocker, exact repro commands, release signing procedure
- `docs/KNOWN_LIMITATIONS.md` — every incomplete or unverified item, explicitly
- `docs/QUALITY_REPORT.md` — consolidated, evidence-only quality summary
- `CHANGELOG.md` — version history

## License / privacy

No telemetry, no analytics, no ads, no network calls, no accounts. All
calculation history and saved items stay in a local, unexported Room
database (see `docs/OFFLINE_AND_PRIVACY.md`).
