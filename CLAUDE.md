# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

For the project overview, tech stack, architecture, and constraints, see [README.md](README.md) —
keep both in sync when either changes.

## Commands

```bash
./gradlew build                                    # full build
./gradlew assembleDebug                             # build debug APK
./gradlew test                                       # unit tests (JVM, JUnit 5 via junit-vintage/launcher)
./gradlew test --tests "org.dmn.template.ExampleUnitTest"   # single unit test class
./gradlew test --tests "org.dmn.template.ExampleUnitTest.addition_isCorrect"  # single test method
./gradlew connectedAndroidTest                        # instrumented tests (device/emulator required)
./gradlew lint                                        # Android lint (warningsAsErrors)
./gradlew spotlessCheck                                # ktlint check (fails on formatting violations)
./gradlew spotlessApply                                # ktlint auto-format
./gradlew detekt                                       # static analysis (default rule set, no formatting)
```

## Agent-specific caveats

- **Never add a `package` declaration to a file under
  `build-logic/convention/src/main/kotlin/`.** Those files (`KotlinAndroid.kt`, `JUnit5Testing.kt`,
  `Spotless.kt`, `Detekt.kt`, `GitHooksConventionPlugin.kt`, `ProjectExtensions.kt`, …) are
  deliberately package-less so `new-project.sh`'s literal `org.dmn.template` text replacement can
  never rewrite a `package` line without moving the file — which would silently desync the package
  from the directory. Keep any new shared build-logic file the same way.
- AGP 9 removed the generic parameterization of `CommonExtension`; DSL blocks like `lint {}` only
  exist on the concrete `ApplicationExtension`/`LibraryExtension` types now. Don't try to add them
  back into the shared `configureKotlinAndroid()` in `KotlinAndroid.kt` — it'll fail to compile.
  Configure them per-plugin instead, as `AndroidApplicationConventionPlugin` /
  `AndroidLibraryConventionPlugin` already do for `lint {}`.
- Spotless's automatic Kotlin source-set detection does not work on Android modules (AGP doesn't
  expose the `SourceSet` API Spotless looks for) — it silently matches zero files without an
  explicit `target(...)`. If you touch `Spotless.kt`, keep the explicit target and verify with a
  deliberately malformed file that `spotlessCheck` actually fails, not just that it runs.
- Running an external process (git, etc.) at Gradle configuration time must go through
  `providers.exec { ... }.result.get()`, not `ProcessBuilder` or `Project.exec` — the latter two
  fail Gradle 9's configuration-cache validation. See `GitHooksConventionPlugin.kt`.
- **Do NOT apply the `org.jetbrains.kotlin.android` plugin.** On AGP 9 / Gradle 9 the
  Android plugin pulls Kotlin support in itself; applying `kotlin.android` breaks the
  build. Keep `kotlin.compose`.
- **Never write a dependency version you have not verified exists.** Android Studio does
  not warn when a version is *higher* than anything ever published (e.g.
  `navigation-compose:2.10.0` does not exist; latest was `2.9.8`) — it only warns about
  outdated ones. Renovate owns version bumps; if you hand-edit `libs.versions.toml`,
  confirm each version actually resolves.
- **Declare the Compose BOM once per independent configuration tree.** `implementation`
  covers debug/release. Do NOT also add the BOM to `debugImplementation` (it extends
  `implementation` → "dependency platform declared multiple times"). `androidTestImplementation`
  and `testImplementation` are separate trees and each need their own BOM copy;
  `ui-test-junit4` needs the BOM on `androidTestImplementation`.
- **Instrumented tests require an API 35+ emulator.** JUnit 6.x has a Java 17 baseline and
  uses `ClassLoader.getDefinedPackage`, which does not exist on the ART runtime below
  API 35 (`NoSuchMethodError: getDefinedPackage`). Unit tests are unaffected (they run on
  JDK 17). To run instrumented tests on older APIs, drop JUnit back to 5.x.
