// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.instrumented.runner) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.convention.git.hooks)
}

// build-logic is a separate included build, so its own spotlessCheck/spotlessApply tasks
// (see build-logic/convention/build.gradle.kts) aren't picked up by an unqualified
// `./gradlew spotlessCheck` unless we depend on them explicitly here.
tasks.register("spotlessCheck") {
    group = "verification"
    description = "Runs spotlessCheck in build-logic as well as every module that has it."
    dependsOn(gradle.includedBuild("build-logic").task(":convention:spotlessCheck"))
}

tasks.register("spotlessApply") {
    group = "verification"
    description = "Runs spotlessApply in build-logic as well as every module that has it."
    dependsOn(gradle.includedBuild("build-logic").task(":convention:spotlessApply"))
}