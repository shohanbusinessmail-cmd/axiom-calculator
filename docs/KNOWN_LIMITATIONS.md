# Known Limitations

This document lists every mandatory requirement that is **not** fully
implemented, is implemented with reduced scope, or could not be verified,
per SECTION 25's audit requirement. Nothing here is hidden elsewhere; the
final report's status claim is bounded by this list.

## 0. The one blocker that affects everything

**No build, test run, lint pass, or APK/AAB was ever produced**, because
this sandbox has no JDK, Android SDK, or Gradle, and no network access to
Google Maven / Maven Central / Gradle's distribution servers (full detail in
`docs/BUILD_AND_RELEASE.md`). Every item below describes a *source-level*
gap; none of the source in this repository has been compiler-verified.

## 1. Standard / Scientific

- **Clipboard copy/paste (STD-009)** is not implemented. The spec requires
  "copy expression," "copy result," and "paste numeric input." This needs
  `androidx.compose.ui.platform.ClipboardManager` wiring in
  `StandardScreen`/`ScientificScreen`, which was not added in this pass.
- **Angle mode (DEG/RAD) does not persist across process restarts.** It is
  stored only in `ScientificViewModel`'s in-memory `StateFlow`, even though
  `SettingsDataStore` already has an `AngleModePreference` key. Wiring these
  together is a small follow-up.
- **Repeated-equals ("chained" operator) behavior** (pressing "=" again
  repeats the last operation with the last operand) is not implemented;
  pressing "=" with no pending expression is a no-op.
- **No dedicated complex-number editor screen.** Complex arithmetic
  (`core/math/Complex.kt`) is fully implemented and tested at the engine
  level, but there is no Scientific-tab UI surface for entering `a+bi` style
  input interactively.
- **Haptic feedback and key sound settings are persisted but not yet wired**
  to actual `HapticFeedback`/`SoundPool` calls on button presses. The
  settings toggle exists and is saved; the keypad does not yet consume it.

## 2. Formulas

- Not every single bullet in SECTION 9 is implemented. Implemented: the full
  percentage family (9 formulas), 4 ratio formulas, 5 finance formulas, 4
  everyday formulas, 13 geometry formulas (circle, rectangle, square,
  triangle, trapezoid, cube, cuboid, sphere, hemisphere, cylinder, cone,
  annulus, parallelogram), 9 physics formulas. **Not implemented:** pyramid
  family, general prism, present-value finance formula, loan amortization
  schedule (only aggregate EMI/total-interest), break-even quantity formula.
- Step-by-step explanations exist only for the percentage family (`steps`
  field); most other formulas return results without an itemized derivation.

## 3. Quick Solve

- **3-variable linear systems**: the solver (`EquationSolver.solveSystem3x3`)
  is implemented and reviewed, but the Quick Solve screen's UI only exposes
  a 2-variable system form; there is no 3x3 input form yet.
- **Polynomial evaluation/root-finding beyond degree 2** and generic
  numerical root finding (bisection/Newton) are not implemented. The spec
  explicitly permits marking this incomplete rather than claiming
  unsupported symbolic/numerical breadth.
- **Factorization** is limited to prime factorization of integers; general
  polynomial factorization is not implemented.
- **Matrix support in Quick Solve's UI** is fixed at 2x2 (the underlying
  `AxMatrix` engine supports arbitrary square dimensions for determinant/
  inverse/rank and arbitrary dimensions for multiply/transpose, but no UI
  exists yet for entering larger matrices).

## 4. Tools

- **SI-prefix reference table (pico→tera)** is not implemented as its own
  tool; only scientific/engineering notation formatting exists.
  Significant-figure rounding and absolute/relative/percentage-error tools
  (SECTION 11 "Engineering tools") are **not implemented**.
- **Unit conversion UI** uses chip-based pickers rather than a dropdown
  search; acceptable for the implemented unit set but not optimized for a
  very large unit list.

## 5. Library

- **Edit-and-recalculate a history entry** is not implemented: a history row
  can be viewed, copied into a saved calculation, or deleted, but tapping it
  does not reopen the originating calculator pre-filled with its input.
- **Undo for destructive deletions** is not implemented; deletions
  (individual, multi-select, clear-all) are immediate and irreversible
  beyond the clear-all confirmation dialog.
- **Reference tab** currently lists core constants and the formula catalog
  titles; it does not yet include the full set of algebra/trigonometric
  identities called for in SECTION 12.
- **No real local export.** Per SECTION 13's explicit instruction ("do not
  create a decorative export button without working export logic"), no
  export feature of any kind is present in Settings or Library — this is a
  deliberate omission, not an oversight, since a correct local
  export/share implementation (e.g. CSV via `Intent.ACTION_SEND` with a
  `FileProvider`) was not built and tested in this pass.

## 6. UI / Accessibility / Animation breadth

- Compose semantics (`contentDescription`) were added to primary interactive
  elements (keys, inputs, result/error text) but not audited exhaustively
  across every screen.
- Reduced-motion preference is persisted in settings but individual
  animations (`AnimatedContent`, `AnimatedVisibility`) do not yet branch on
  it to disable themselves.
- Tablet/landscape/foldable adaptive layouts (navigation rail instead of
  bottom bar on large screens) are not implemented; only a phone-sized
  bottom-navigation layout exists.
- Dynamic color (Material You) is intentionally not used (see
  `ui/theme/Theme.kt` comment) to keep the brand identity consistent; this
  is a deliberate design choice, not a gap.

## 7. Testing / Verification

- No Compose UI tests or instrumented (`androidTest`) tests were written in
  this pass (dependencies are declared and ready for them).
- No property-based testing library is included; invariant-style checks
  (round-trip conversions, identity-matrix multiplication) are covered by
  targeted example-based JUnit tests instead.
- All JUnit tests under `app/src/test/kotlin` are written but **unexecuted**
  (see the blocker in §0). They must be run with `./gradlew test` by a
  maintainer with a working toolchain before being trusted as "passing."

## 8. Release

- The release build has **no signing configuration** (SECTION 21 forbids
  committing signing keys); an unsigned/default-debug-signed artifact must
  never be called a production release. See `docs/BUILD_AND_RELEASE.md` §2.4
  for the exact manual step a maintainer must take.
- No APK/AAB exists in this repository or was ever generated, for the
  reasons in §0.
