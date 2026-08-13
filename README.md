# Android Compose Template

An opinionated GitHub template for bootstrapping a new Android app. It exists so that starting a
new project means writing features on day one, not re-deciding and re-wiring the same dozen
things every time: Compose + Material 3, DI, a test setup, a curated dependency catalog, and a
code-hygiene pipeline (lint, formatting, static analysis, a pre-commit hook) - all already in
place and already talking to each other.

It ships intentionally minimal: a single `:app` module, no feature code, no screens beyond the
default. You rename it, then build your app inside it.

## Tech stack

| Concern              | Choice                                                                                                                   |
|-----------------------|--------------------------------------------------------------------------------------------------------------------------|
| UI                    | Jetpack Compose + Material 3                                                                                             |
| DI                    | [Koin](https://insert-koin.io/) (BOM-managed) - not Hilt/Dagger                                                          |
| Testing               | JUnit 5 + MockK + Turbine + Robolectric (incl. AndroidX Test, coroutines test, Compose UI testing, Koin Test)            |
| Build config          | Gradle **convention plugins** in `build-logic/`, not inline in module scripts                                            |
| Dependency versions   | A single Gradle version catalog (`gradle/libs.versions.toml`), kept current by [Renovate](https://docs.renovatebot.com/) |
| Formatting            | [Spotless](https://github.com/diffplug/spotless) + ktlint                                                                |
| Static analysis       | [detekt](https://detekt.dev/)                                                                                            |

## Getting started

1. Click **"Use this template"** on GitHub to create your own repo from this one.
2. Clone it, then rename the template to your app:

   ```bash
   ./new-project.sh com.acme.notes "Acme Notes"
   rm -rf .git && git init && git add -A && git commit -m "Initial commit"
   ```

   `new-project.sh` does a literal find-and-replace of `org.dmn.template` → your applicationId and
   `AndroidTemplate` → your app name across every tracked text file, then moves the Kotlin source
   directories to match the new package path. It does **not** rename the base theme
   (`Theme.AndroidTemplate` in `themes.xml` / `values-night/themes.xml`) - do that by hand if you
   want the theme name to match your new app name.

3. Build it:

   ```bash
   ./gradlew build
   ```

## Everyday commands

```bash
./gradlew assembleDebug                                      # build a debug APK
./gradlew test                                                # unit tests (JVM, JUnit 5)
./gradlew test --tests "org.dmn.template.ExampleUnitTest"      # single test class
./gradlew connectedAndroidTest                                 # instrumented tests (device/emulator required)
./gradlew lint                                                 # Android lint
./gradlew spotlessCheck                                        # ktlint check (fails on formatting violations)
./gradlew spotlessApply                                        # ktlint auto-format
./gradlew detekt                                                # static analysis
```

A pre-commit hook already runs `spotlessCheck` for you (see [Code hygiene](#code-hygiene) below) -
these are for running things manually or in CI.

## Architecture

### Convention plugins own the build config

Nothing about compileSdk, minSdk, the Kotlin toolchain, test wiring, or Compose setup is written
inline in `app/build.gradle.kts`. It's all centralized in `build-logic/` - a separate,
[included build](https://docs.gradle.org/current/userguide/composite_builds.html) that defines two
plugins:

- `AndroidApplicationConventionPlugin` (id `convention.android.application`)
- `AndroidLibraryConventionPlugin` (id `convention.android.library`)

A module opts into all of it with one line:

```kotlin
plugins {
    alias(libs.plugins.convention.android.application)
}
```

`app/build.gradle.kts` does exactly that, then sets only what's genuinely per-module: namespace,
applicationId, versionCode, versionName. Everything else - source layout, JUnit 5 wiring, lint,
Spotless, detekt, the Compose runtime dependency list - comes from the convention plugin.

The library convention plugin exists for when you need it - today the template only has the `:app`
module. **Adding a new library module** is just: create the module, apply
`alias(libs.plugins.convention.android.library)`, done. No new convention-plugin work needed.

The shared configuration logic lives in plain `.kt` files under
`build-logic/convention/src/main/kotlin/` - `KotlinAndroid.kt`, `JUnit5Testing.kt`, `Spotless.kt`,
`Detekt.kt`, `GitHooksConventionPlugin.kt`, `ProjectExtensions.kt` - each with **no package
declaration**. That's deliberate: `new-project.sh`'s literal text replacement could otherwise
rewrite a `package org.dmn.template` line without moving the file, silently desyncing the package
from its directory. Keep new shared build-logic files package-less for the same reason.

### Non-standard Kotlin source layout

Every Android module's source sets point at `src/<name>/kotlin` instead of Gradle's default
`src/<name>/java` - a Kotlin-first layout matching the project's all-Kotlin codebase. This is set
once, for every module, in `KotlinAndroid.kt`, so you don't need to configure it per module.

### Version catalog is the single source of truth

All dependency and plugin versions live in `gradle/libs.versions.toml`. Both `app/build.gradle.kts`
and `build-logic` read from the same file (`build-logic`'s own `versionCatalogs` block points back
at the root catalog - there's only ever one). Add new dependencies there, not as inline coordinates
in a build script.

### Testing

Unit tests (`src/test`) and instrumented tests (`src/androidTest`) both run on **JUnit 5**, via the
`de.mannodermaus.junit5` (`android-junit5`) Gradle plugin. JUnit 4 stays alive underneath for
Compose UI testing (`ui-test-junit4`) and via the JUnit 5 vintage engine, so both styles of test
work. All of it is wired automatically by the convention plugins - you don't add any of this
per-module. See the [Tech stack](#tech-stack) table above for the full list of testing libraries.

### Code hygiene

Lint, Spotless, and detekt are wired into the same convention plugins as everything else, so any
module that applies `convention.android.application` / `.library` gets all of it automatically.

- **Lint** runs with `warningsAsErrors = true` - a lint warning fails your build, the same as an
  error. The one exception is `GradleDependency` (the "a newer version is available" check), which
  is disabled: dependency freshness is Renovate's job, not lint's.
- **Spotless + ktlint** is the only formatter (`ktlint_official` code style, enforced via the root
  `.editorconfig`). Run `./gradlew spotlessApply` to auto-fix formatting before you fight the CI
  about it.
- **detekt** runs static analysis only - no auto-formatting, so it never fights with ktlint over
  the same lines. It uses detekt's default rule set with no baseline: violations need to be fixed,
  not grandfathered in.
- **A tracked pre-commit hook** (`.githooks/pre-commit`) runs `spotlessCheck` before every commit.
  It activates itself automatically - the first time you run any `./gradlew` command after cloning,
  a small convention plugin points Git's `core.hooksPath` at `.githooks/`. No manual setup step.

#### Indent size and max line length

`max_line_length` is set to 90, a bit more room than the traditional 80-column standard.
`indent_size` is set to 8, double the usual 4. Together they push toward shorter functions and
shallower nesting: each nesting level costs `indent_size` columns before any actual code starts,
so a tight `max_line_length` combined with a wide `indent_size` leaves little room once you're a
few blocks deep.

Both are set in the root [`.editorconfig`](.editorconfig), which ktlint (via Spotless) reads
directly:

```ini
[*]
indent_size = 8

[*.{kt,kts}]
max_line_length = 90
```

`indent_size` applies to every file type (`[*]`), `max_line_length` is Kotlin/Kotlin-script only.
Change either by editing `.editorconfig`, then run `./gradlew spotlessApply` to reformat everything
to match - don't hand-format to a value the config doesn't say.

If you tighten either further, run `spotlessApply` and check it actually converges - ktlint can
end up unable to auto-wrap a line at all (it'll fail instead of looping forever), and you'll need
to rewrap that line by hand before `spotlessApply` can finish reformatting the rest of the file.

### A few things AGP/Gradle make you get right

- `compileSdk` is declared through AGP's newer typed API
  (`compileSdk { version = release(37) { ... } }`), not a plain integer. This only exists on recent
  AGP versions.
- R8/keep rules live under `app/src/main/keepRules/*.keep` (AGP's newer keep-rules directory
  convention) rather than a single root `proguard-rules.pro`. The `release` build type currently
  has optimization disabled (`optimization { enable = false }`).
- `dependencyResolutionManagement` is set to `FAIL_ON_PROJECT_REPOS` in `settings.gradle.kts` -
  don't add `repositories {}` blocks inside module build scripts; declare repositories in
  `settings.gradle.kts` only, or the build will fail.

## Keeping it current

[Renovate](https://docs.renovatebot.com/) is configured (`renovate.json`) to open PRs as
dependencies and plugins release new versions, so the catalog doesn't quietly rot.
