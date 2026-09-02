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
   ./gradlew renameProject --namespace=com.acme.notes --app-name="Acme Notes"
   rm -rf .git && git init && git add -A && git commit -m "Initial commit"
   ```

   `renameProject` reads the current namespace/applicationId straight from an application
   module's AGP extension and the current project name from `rootProject.name` in
   `settings.gradle.kts`, then does a literal find-and-replace across every tracked text file.
   Source directories are moved using each subproject's *actual configured* Kotlin/Java source
   sets (read from AGP, one subproject at a time) - not a guessed `src/main/kotlin` layout - so it
   correctly follows `main`/`test`/`androidTest`, any build-type or flavor source set (`debug`,
   `demoDebug`, ...), a `java/`-only layout, and any library module beyond `:app`, wherever those
   directories actually live. The one identifier-sensitive spot - the base theme name
   (`Theme.AndroidTemplate` in `themes.xml` / `values-night/themes.xml` / `AndroidManifest.xml`) -
   is renamed using a space-stripped version of `--app-name`, since Android resource names can't
   contain spaces; `app_name` in `strings.xml` still gets exactly what you typed.

   All flags - `--namespace`, `--application-id`, `--app-name` - are independent and optional;
   pass just the one(s) you want to change (e.g. `--app-name="Acme Notes"` alone renames only the
   display name). `--application-id` defaults to `--namespace` when the two started in sync, which
   is the common case since the template ships with them equal - pass `--application-id`
   explicitly only when you want it to diverge from the namespace (e.g. a namespace with more
   segments than the applicationId). The template ships with a single `:app` module, so
   `renameProject` finds it automatically; if you add a second `com.android.application` module,
   pass `--app-module=:app` (or whichever project path) to say which one's applicationId to patch
   - the namespace/app-name substitution still applies repo-wide either way.

   From Android Studio instead of the terminal: **Run → Edit Configurations → + → Gradle**, and set
   the task/arguments field to
   `renameProject --namespace=com.acme.notes --app-name="Acme Notes"`.

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
[included build](https://docs.gradle.org/current/userguide/composite_builds.html) with two
modules, split by what the code is *about* rather than just grouped together:

- `:convention` - config for Android modules / the app itself: `AndroidApplicationConventionPlugin`
  (id `convention.android.application`) and `AndroidLibraryConventionPlugin`
  (id `convention.android.library`).
- `:project-tasks` - repo-maintenance tooling: it doesn't *configure* how a module builds the way
  `:convention` does, but these tasks need to read module configuration (namespace, applicationId,
  source sets) back out via the AGP DSL, so this module depends on
  `com.android.tools.build:gradle-api` too. `GitHooksConventionPlugin`
  (id `project-tasks.git-hooks`), `RenameProjectConventionPlugin`
  (id `project-tasks.rename-project`, registers the `renameProject` task used in
  [Getting started](#getting-started)), and `AddLibraryModuleConventionPlugin`
  (id `project-tasks.add-library-module`, registers the `addLibraryModule` task described below) -
  the latter two inspect every subproject after Gradle's configuration phase finishes
  (`gradle.projectsEvaluated`), so they pick up any module you add later without code changes; with
  more than one `com.android.application` module, `renameProject` needs `--app-module=<path>` to
  know which one's applicationId to anchor on and patch, and `addLibraryModule` needs an explicit
  `--namespace` since it can no longer derive a single default.

A module opts into the Android config with one line:

```kotlin
plugins {
    alias(libs.plugins.convention.android.application)
}
```

`app/build.gradle.kts` does exactly that, then sets only what's genuinely per-module: namespace,
applicationId, versionCode, versionName. Everything else - source layout, JUnit 5 wiring, lint,
Spotless, detekt, the Compose runtime dependency list - comes from the convention plugin.

The library convention plugin exists for when you need it - today the template only has the `:app`
module. **Adding a new library module** is a single command:

```bash
./gradlew addLibraryModule --name=network
```

`addLibraryModule` scaffolds `<name>/build.gradle.kts` (already wired to
`alias(libs.plugins.convention.android.library)`), `<name>/.gitignore`,
`<name>/src/main/kotlin/<package>/` (empty, ready for your code), and a JUnit 5
`<name>/src/test/kotlin/<package>/ExampleUnitTest.kt`, then appends `include(":<name>")` to
`settings.gradle.kts`. It doesn't generate an `AndroidManifest.xml` - AGP resolves a library
module's `namespace` from the DSL, so an empty module doesn't need one; add one by hand only if the
module ends up needing to declare permissions, providers, or other manifest entries.

`--namespace` defaults to `<app namespace>.<name>` (e.g. `org.dmn.template.network`), read from the
`:app` module the same way `renameProject` reads it - pass `--namespace=com.acme.notes.network`
explicitly to override it, or when there's no single application module to default from. `--name`
must be a valid Gradle project name (letters, digits, hyphens, starting with a letter) and only
creates a flat top-level module - nested paths like `core:network` aren't supported. As with
`renameProject`, editing `settings.gradle.kts` doesn't affect the Gradle invocation that's already
running, so re-sync / re-run Gradle after the task finishes to pick up the new module.

The shared configuration logic lives in plain `.kt` files under each module's
`src/main/kotlin/` - `build-logic/convention/` has `KotlinAndroid.kt`, `JUnit5Testing.kt`,
`Spotless.kt`, `Detekt.kt`, `ProjectExtensions.kt`; `build-logic/project-tasks/` has
`GitHooksConventionPlugin.kt`, `RenameProjectConventionPlugin.kt`, `RenameProjectTask.kt`,
`AddLibraryModuleConventionPlugin.kt`, `AddLibraryModuleTask.kt` - and every one of them has **no
package declaration**. That's deliberate: `renameProject`'s literal text replacement could
otherwise rewrite a `package org.dmn.template` line without moving the file, silently desyncing the
package from its directory. Keep new shared build-logic files package-less for the same reason.

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
