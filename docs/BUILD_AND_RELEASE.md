# Build and Release

## 0. Critical environment blocker (read this first)

The coding-agent sandbox used to author this repository has **no JDK, no
Android SDK, no Gradle distribution, and no network access to the servers
required to fetch any of them or any Android/Kotlin Gradle dependency.**

Verified at the start of this work session:

| Check | Result |
|---|---|
| `java -version` | `command not found` — no JDK installed |
| `$ANDROID_HOME` / `$ANDROID_SDK_ROOT` | unset; no SDK directory present |
| `which gradle` | not found; no cached Gradle distribution |
| `apt-get update` | fails — no reachable Debian mirror |
| `curl https://dl.google.com` (Google Maven) | connection refused (host unreachable) |
| `curl https://repo1.maven.org` (Maven Central) | connection refused (host unreachable) |
| `curl https://services.gradle.org` (Gradle distributions) | connection refused (host unreachable) |
| GitHub release asset download (redirects to `objects.githubusercontent.com`) | connection refused (host unreachable) |

The sandbox's outbound network allowlist is limited to `github.com`,
`codeload.github.com`, `api.github.com`, `registry.npmjs.org`, and
`pypi.org`/`files.pythonhosted.org`. None of these host a JDK, the Android
SDK, Gradle, or the AndroidX/Compose/Room/Kotlin artifacts this project's
`gradle/libs.versions.toml` declares.

**Consequence: none of the following could be executed in this environment,
and none of their results are fabricated anywhere in this repository or in
the final report:**
- `./gradlew --version`, `./gradlew tasks`
- `./gradlew assembleDebug`, `./gradlew assembleRelease`
- `./gradlew test`, `./gradlew lint`, `./gradlew connectedDebugAndroidTest`
- Generation of any `.apk` or `.aab` file
- Installing the app on a device/emulator (none is available either)

This is a **genuine external environmental blocker**, not a decision to skip
verification. Per the engineering specification's own governing rule
("if a genuine environmental blocker prevents an operation, complete all
remaining feasible work, document the blocker, and continue wherever safe"),
the project proceeded to implement the complete Android/Kotlin source tree,
and substituted an independently executable verification strategy for the
mathematical core (see `docs/QUALITY_REPORT.md` and
`scripts/verification/`), which **could** be run in this sandbox because it
only requires Python.

## 1. What IS verified

- `scripts/verification/engine_reference.py` + `run_vectors.py`: a structural
  Python port of the tokenizer/parser/evaluator/solvers, executed against
  every mandatory SECTION 17 vector. Result: **113/113 passed** (see
  `scripts/verification/reference_vector_results.txt`, reproducible by
  running `python3 scripts/verification/run_vectors.py`).
- Every Kotlin source file was written as a direct structural port of this
  verified Python logic (same grammar, same precedence rules, same
  documented ambiguity resolutions, same error conditions).
- Static, manual review of the Gradle configuration, manifest, and
  dependency graph for the offline/privacy posture (`docs/OFFLINE_AND_PRIVACY.md`).

## 2. What is NOT verified (and must be done by a maintainer with a real toolchain)

1. **Kotlin compiles cleanly.** The source was written carefully and
   reviewed manually, but no compiler ever parsed it. Run:
   ```
   ./gradlew assembleDebug
   ```
   and fix any compilation errors. (If `gradlew`'s wrapper jar is missing —
   see note below — run `gradle wrapper --gradle-version 8.9` once with a
   locally installed Gradle to generate it.)
2. **JUnit tests actually pass under Gradle/JVM.** Run:
   ```
   ./gradlew test
   ```
   The Kotlin test suite under `app/src/test/kotlin` is a structural port of
   the vectors already proven correct in Python, but it has never been
   executed by a real JUnit runner.
3. **Lint.** Run `./gradlew lint` and address any findings.
4. **Release build + signing.** Run `./gradlew assembleRelease`. The release
   `buildType` has **no `signingConfig` configured** (SECTION 21: "Do not
   commit signing keys"), so the output APK is **unsigned** until a
   maintainer supplies a release keystore locally, e.g.:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file(System.getenv("AXIOM_KEYSTORE_PATH") ?: "release.keystore")
           storePassword = System.getenv("AXIOM_KEYSTORE_PASSWORD")
           keyAlias = System.getenv("AXIOM_KEY_ALIAS")
           keyPassword = System.getenv("AXIOM_KEY_PASSWORD")
       }
   }
   ```
   and assign `signingConfig = signingConfigs.getByName("release")` to the
   `release` buildType. **No keystore, password, or key is present anywhere
   in this repository.**
5. **Instrumented/Compose UI tests and device/emulator verification.** No
   emulator or device is available in this sandbox.

## 3. Gradle wrapper note

`gradle/wrapper/gradle-wrapper.properties` is present and points at Gradle
8.9. The remaining wrapper artifacts — the `gradlew`/`gradlew.bat`
launcher scripts and the binary bootstrap JAR (`gradle-wrapper.jar`) — are
**not present in this repository**. They are normally generated by running
`gradle wrapper` once against a locally installed Gradle; that command
itself requires a working Gradle installation (or network access to fetch
one), neither of which this sandbox has. A maintainer with a locally
installed Gradle and/or normal internet access can generate all three
missing files with:
```
gradle wrapper --gradle-version 8.9 --distribution-type bin
```
or simply open the project in Android Studio, which regenerates them
automatically. Until then, the exact commands in this document (`./gradlew
...`) must be read as "the gradle wrapper command that will exist once the
wrapper is regenerated," not as something that can be run against this
checkout as-is.

## 4. Toolchain this project targets

- Android Gradle Plugin 8.5.2, Kotlin 2.0.20, KSP 2.0.20-1.0.25
- `compileSdk`/`targetSdk` 34, `minSdk` 26 (Android 8.0 — chosen so the
  adaptive launcher icon and `enableOnBackInvokedCallback` can be relied on
  without legacy fallbacks)
- JVM target 17
- See `gradle/libs.versions.toml` for the exact dependency versions selected.
