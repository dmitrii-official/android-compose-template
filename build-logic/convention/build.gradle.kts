import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
        `kotlin-dsl`
        alias(libs.plugins.spotless)
}

group = "org.dmn.template.buildlogic"

java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
        compilerOptions {
                jvmTarget = JvmTarget.JVM_17
        }
}

dependencies {
        compileOnly(libs.android.gradlePlugin)
        compileOnly(libs.kotlin.gradlePlugin)
        compileOnly(libs.compose.gradlePlugin)
        compileOnly(libs.android.junit5.gradlePlugin)
        compileOnly(libs.spotless.gradlePlugin)
        compileOnly(libs.detekt.gradlePlugin)
}

gradlePlugin {
        plugins {
                register("androidApplication") {
                        id =
                                libs.plugins.convention.android.application
                                        .get()
                                        .pluginId
                        implementationClass = "AndroidApplicationConventionPlugin"
                }
                register("androidLibrary") {
                        id =
                                libs.plugins.convention.android.library
                                        .get()
                                        .pluginId
                        implementationClass = "AndroidLibraryConventionPlugin"
                }
                register("gitHooks") {
                        id =
                                libs.plugins.convention.git.hooks
                                        .get()
                                        .pluginId
                        implementationClass = "GitHooksConventionPlugin"
                }
        }
}

extensions.configure<SpotlessExtension> {
        kotlin {
                target("src/**/*.kt")
                targetExclude("**/build/**/*.kt")
                ktlint(libs.versions.ktlint.get())
        }
        kotlinGradle {
                target("*.gradle.kts")
                ktlint(libs.versions.ktlint.get())
        }
}

tasks
        .compileKotlin
        .get()
        .compilerOptions {
                freeCompilerArgs.set(listOf("-Xcontext-parameters"))
        }
