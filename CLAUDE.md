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

There is no `build-logic`/convention-plugins module yet — all Gradle config lives directly in the
root `build.gradle.kts` and `app/build.gradle.kts`, driven by `gradle/libs.versions.toml`.

## Architecture notes

- **Single module (`:app`)**, namespace/applicationId `org.dmn.template`. Non-standard Kotlin
  source layout: each source set points at `src/<name>/kotlin` instead of the default `src/<name>/java`
  (set via `sourceSets { all { kotlin.directories... } }` in [app/build.gradle.kts](app/build.gradle.kts)).
- **Version catalog is the single source of truth** for dependency/plugin versions
  ([gradle/libs.versions.toml](gradle/libs.versions.toml)). Add new dependencies there, not as
  inline coordinates in a build script.
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
