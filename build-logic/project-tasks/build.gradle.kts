import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
        `kotlin-dsl`
        alias(libs.plugins.spotless)
}

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
}

gradlePlugin {
        plugins {
                register("gitHooks") {
                        id =
                                libs.plugins.project.tasks.git.hooks
                                        .get()
                                        .pluginId
                        implementationClass = "GitHooksConventionPlugin"
                }
                register("renameProject") {
                        id =
                                libs.plugins.project.tasks.rename.project
                                        .get()
                                        .pluginId
                        implementationClass = "RenameProjectConventionPlugin"
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
