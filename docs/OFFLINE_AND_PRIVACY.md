# Offline and Privacy

## 1. Manifest inspection (static)

`app/src/main/AndroidManifest.xml` declares:
- **No `<uses-permission>` elements at all.** In particular, there is no
  `android.permission.INTERNET` and no network-related permission of any
  kind.
- `android:allowBackup="false"` with an explicit `data_extraction_rules.xml`
  that excludes both the `database` and `sharedpref` domains from cloud
  backup and device-to-device transfer, even though `allowBackup=false`
  already disables both paths. This makes the backup policy self-documenting
  rather than implicit: **no application data leaves the device via the
  Android backup subsystem**, by explicit configuration, not by omission.

## 2. Dependency graph inspection (static)

`gradle/libs.versions.toml` / `app/build.gradle.kts` declare only:
AndroidX Core/Lifecycle/Activity/Compose/Navigation, Room (local SQLite
persistence), DataStore Preferences (local key-value persistence), and
Kotlin Coroutines. There is:
- No analytics SDK (Firebase Analytics, Mixpanel, Amplitude, etc.)
- No crash-reporting SDK that transmits data off-device (Crashlytics, Sentry, etc.)
- No advertising SDK (AdMob, etc.)
- No remote-config SDK
- No networking client (Retrofit, OkHttp, Ktor client, Volley, etc.)
- No payment/subscription SDK
- No WebView-based remote content loading

## 3. Runtime code path inspection (static)

A repository-wide search for network-capable APIs
(`HttpURLConnection`, `okhttp3`, `java.net.Socket`, `WorkManager` network
constraints, `WebView`) finds no usage anywhere in `app/src/main/kotlin`.
All six tabs' ViewModels call only local `core/*` pure functions and local
`data/repository/*` classes backed by Room/DataStore.

## 4. Runtime (on-device) offline verification

**Not executed.** This sandbox has no Android device, emulator, or means to
install an APK (see `docs/BUILD_AND_RELEASE.md` for the full explanation).
The SECTION 22 runtime checklist (install with networking disabled; verify
startup, standard arithmetic, scientific calculation, formula calculation,
unit conversion, history access, preference persistence, and restart) has
**not** been performed and must be completed by a maintainer with a real
device/emulator before shipping. Only the static inspections in sections 1–3
above were actually carried out, and this document deliberately does not
claim more than that.

## 5. Data storage summary

| Data | Storage | Retention |
|---|---|---|
| Calculation history | Room (`history` table) | Auto-deletes per the configured retention policy (1/7/30/90 days or Never); default 30 days |
| Saved/favorited calculations | Room (`saved_calculations` table) | Never auto-deleted; only explicit user deletion |
| Preferences (theme, angle mode, precision, notation, grouping, haptics, key sound, reduced motion, retention policy, calculator memory) | DataStore Preferences | Until explicitly changed/cleared by the user |

All of the above live exclusively in the app's private local storage
(`/data/data/com.axiom.calculator/...` on-device) and are never transmitted
anywhere, consistent with the manifest having no `INTERNET` permission.
