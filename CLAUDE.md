# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A GitHub template repo for bootstrapping Android apps: Jetpack Compose + Material 3, Koin DI, a
curated Gradle version catalog, and a JUnit 5 test setup. It is intentionally minimal (single
`:app` module, no feature code yet) so it can be renamed and built out per-project. Kept current
by Renovate (see `renovate.json`).

## Turning the template into a real project

```bash
./new-project.sh com.acme.notes "Acme Notes"
rm -rf .git && git init && git add -A && git commit -m "Initial commit"
```

`new-project.sh` does a literal find/replace of `org.dmn.template` -> new applicationId and
`AndroidTemplate` -> new app name across all tracked text files, then moves the Kotlin source
dirs (`main`, `test`, `androidTest`) to match the new package path.

## Commands

```bash
./gradlew build                                    # full build
./gradlew assembleDebug                             # build debug APK
./gradlew test                                       # unit tests (JVM, JUnit 5 via junit-vintage/launcher)
./gradlew test --tests "org.dmn.template.ExampleUnitTest"   # single unit test class
./gradlew test --tests "org.dmn.template.ExampleUnitTest.addition_isCorrect"  # single test method
./gradlew connectedAndroidTest                        # instrumented tests (device/emulator required)
./gradlew lint                                        # Android lint
```

Gradle config lives in a `build-logic` included build ([build-logic/convention](build-logic/convention)),
not inline in module build scripts. `app/build.gradle.kts` just applies
`convention.android.application` and sets its own identity (namespace/applicationId/versionCode/versionName).

## Architecture notes

- **Convention plugins** (`build-logic/convention/src/main/kotlin`): `AndroidApplicationConventionPlugin`
  and `AndroidLibraryConventionPlugin` own everything else — compileSdk/minSdk, the custom
  `src/<name>/kotlin` source layout, JVM toolchain, JUnit 5 test runner wiring, and (application only)
  Compose setup + the full runtime dependency list. Shared logic lives in plain `.kt` files with
  **no package declaration** (`KotlinAndroid.kt`, `JUnit5Testing.kt`, `ProjectExtensions.kt`) —
  deliberate, so `new-project.sh`'s literal `org.dmn.template` replacement can never rewrite a
  `package` line without moving the file, which would silently desync package from directory. The
  plugin IDs (`convention.android.application` / `.library`, registered in
  [gradle/libs.versions.toml](gradle/libs.versions.toml)'s `[plugins]` table with no version) are
  also intentionally project-agnostic so a rename never needs to touch them. Adding a library
  module means creating it and applying `alias(libs.plugins.convention.android.library)` — no new
  convention-plugin work needed.
- **Single app module (`:app`)** today, namespace/applicationId `org.dmn.template`. Non-standard
  Kotlin source layout: each source set points at `src/<name>/kotlin` instead of the default
  `src/<name>/java` (set once, for all Android modules, in `KotlinAndroid.kt`).
- **Version catalog is the single source of truth** for dependency/plugin versions
  ([gradle/libs.versions.toml](gradle/libs.versions.toml)), consumed both by `app/build.gradle.kts`
  and by `build-logic` (which points its own `versionCatalogs` block back at the same root file —
  there is only ever one catalog). Add new dependencies there, not as inline coordinates in a
  build script.
- **Dual test runner setup**: unit tests (`src/test`) and instrumented tests (`src/androidTest`)
  both run on JUnit 5 via the `de.mannodermaus.junit5` (`android-junit5`) plugin, with JUnit 4
  kept alive underneath for Compose UI testing (`ui-test-junit4`) and via the vintage engine.
  `testOptions.unitTests.all { it.useJUnitPlatform() }` is required for unit tests to pick up
  JUnit 5.
- **DI**: Koin (BOM-managed), not Hilt/Dagger. `koin-android` + `koin-androidx-compose` for the
  app, `koin-test-junit5` for tests.
- **AGP `compileSdk` is declared via the new typed API** (`compileSdk { version = release(37) { ... } }`),
  not a plain integer — this only exists on recent AGP.
- **R8/keep rules** live under `app/src/main/keepRules/*.keep` (AGP's newer keep-rules directory
  convention), not a single root `proguard-rules.pro`. `release` build type currently has
  optimization disabled (`optimization { enable = false }`).
- **Repo hygiene**: `dependencyResolutionManagement` is `FAIL_ON_PROJECT_REPOS` — do not add
  `repositories {}` blocks inside module build scripts; add repos in `settings.gradle.kts` only.
- Base theme is `Theme.AndroidTemplate` (in `themes.xml` / `values-night/themes.xml`, referenced
  from `AndroidManifest.xml`). `new-project.sh` does not rename theme style names, so rename
  `Theme.AndroidTemplate` manually if you want the theme to match your new app name.
