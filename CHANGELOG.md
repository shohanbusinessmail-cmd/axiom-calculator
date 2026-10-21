# Changelog

All notable changes to Axiom Calculator are documented here. Format loosely
follows [Keep a Changelog](https://keepachangelog.com/). This project has
not yet had a built/tested/signed release — see `docs/BUILD_AND_RELEASE.md`.

## [1.0.0-rc1] — Unreleased (source-complete, unbuilt)

Initial source-complete pass implementing the full six-tab calculator
described in the project spec, from an empty repository.

### Added
- **Standard tab**: BigDecimal-based arithmetic engine, operator precedence,
  contextual percent, memory (MC/MR/M+/M-/MS, DataStore-persisted),
  undo/redo, typed error handling (no crashes on divide-by-zero/malformed
  input).
- **Scientific tab**: trig/hyperbolic/log/exp/power/root/factorial/nPr/nCr,
  complex number arithmetic (`Complex.kt`), fraction arithmetic
  (`Fraction.kt`), matrix arithmetic (`Matrix.kt`), DEG/RAD mode,
  scientific/engineering notation formatting.
- **Formulas tab**: searchable formula registry covering percentage (9),
  ratio (4), finance incl. EMI (5), everyday (4), geometry (13), and physics
  (9) formulas, each with typed inputs/outputs and domain-error handling.
- **Quick Solve tab**: linear and quadratic equation solving (real + complex
  roots), 2-variable linear systems (Cramer's rule), 3-variable system
  solving at the engine level, number theory (GCD/LCM/primality/
  factorization), arithmetic/geometric sequences, descriptive statistics,
  matrix operations.
- **Tools tab**: unit conversion (length/area/volume/mass/time/speed/
  pressure/energy/power + affine temperature conversion), programmer
  calculator (base conversion, bitwise AND/OR/XOR/NOT, explicit bit width,
  two's-complement, logical vs. arithmetic shifts), date math (difference,
  add/subtract with end-of-month clamping, leap-year rules), BMI/BMR.
- **Library tab**: Room-backed calculation history with configurable
  retention (1/7/30/90 days/Never, default 30 — injectable-clock cleanup
  rule), structurally separate saved/favorite calculations, formula/constant
  reference list.
- **Settings**: theme (system/light/dark), angle mode, precision/notation,
  haptics/key-sound/reduced-motion toggles, retention policy control.
- **About**: app identity, version, developer contact, privacy summary.
- Manual dependency injection via `AppContainer` (no Hilt/Dagger).
- Full offline/privacy posture: zero `<uses-permission>` entries, no
  analytics/ads/network dependencies, `allowBackup=false` with explicit
  `data_extraction_rules.xml`.
- Documentation set: `docs/ARCHITECTURE.md`, `docs/MATHEMATICAL_SEMANTICS.md`,
  `docs/FEATURE_REGISTRY.md`, `docs/TEST_STRATEGY.md`,
  `docs/OFFLINE_AND_PRIVACY.md`, `docs/BUILD_AND_RELEASE.md`,
  `docs/KNOWN_LIMITATIONS.md`, `docs/QUALITY_REPORT.md`.
- `scripts/verification/` — standalone Python reference implementation of
  the math engine plus a runner covering every mandatory SECTION 17 vector;
  executed with result 113/113 passed.
- 10 JUnit test files under `app/src/test/kotlin` covering the math engine,
  solve engines, unit converter, programmer tools, date math, formula
  registry, and the history repository's retention logic. **Written but not
  executed** — no JDK/Gradle toolchain was available in the development
  environment (see `docs/BUILD_AND_RELEASE.md`).

### Known gaps (see `docs/KNOWN_LIMITATIONS.md` for the full list)
- No build/test/lint was ever run; no APK/AAB exists.
- No clipboard copy/paste, no edit-and-recalculate from history, no local
  export, no 3x3-system or complex-number dedicated UI surfaces, no
  polynomial root finding beyond degree 2, no pyramid/prism geometry
  formulas, no SI-prefix/significant-figure tools, no instrumented/Compose
  UI tests, no tablet/landscape adaptive layout, haptics/sound settings
  persisted but not yet wired to actual key-press feedback.
- Release build has no signing configuration (by design — no keystore is
  committed).

## [Unreleased]
Nothing pending beyond the items in Known Limitations.
