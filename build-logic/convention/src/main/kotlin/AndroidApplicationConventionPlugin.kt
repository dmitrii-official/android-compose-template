import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

abstract class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
            apply(plugin = "de.mannodermaus.android-junit5")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig.targetSdk = 37

                buildFeatures.compose = true

                buildTypes.getByName("release").apply {
                    optimization {
                        enable = false
                    }
                }
            }

            configureJUnit5Testing()

            dependencies {
                "implementation"(libs.findLibrary("androidx.appcompat").get())
                "implementation"(libs.findLibrary("androidx.core.ktx").get())
                "implementation"(libs.findLibrary("material").get())

                "implementation"(platform(libs.findLibrary("androidx.compose.bom").get()))
                "implementation"(libs.findLibrary("androidx.activity.compose").get())

                "implementation"(libs.findLibrary("androidx.compose.material3").get())
                "implementation"(libs.findLibrary("androidx.compose.ui").get())
                "implementation"(libs.findLibrary("androidx.compose.ui.graphics").get())
                "implementation"(libs.findLibrary("androidx.compose.ui.tooling.preview").get())
                "implementation"(libs.findLibrary("androidx.lifecycle.runtime.ktx").get())

                "implementation"(platform(libs.findLibrary("koin.bom").get()))
                "implementation"(libs.findLibrary("koin.android").get())
                "implementation"(libs.findLibrary("koin.androidx.compose").get())
                "implementation"(libs.findLibrary("kotlinx.coroutines.android").get())
                "implementation"(libs.findLibrary("androidx.navigation.compose").get())
                "implementation"(libs.findLibrary("androidx.compose.ui.text.google.fonts").get())
                "implementation"(libs.findLibrary("timber").get())
                "implementation"(libs.findLibrary("androidx.compose.material.icons.extended").get())

                "testImplementation"(libs.findLibrary("koin.test.junit5").get())

                "androidTestImplementation"(platform(libs.findLibrary("androidx.compose.bom").get()))
                "androidTestImplementation"(libs.findLibrary("androidx.compose.ui.test.junit4").get())

                "debugImplementation"(libs.findLibrary("androidx.compose.ui.tooling").get())
                "debugImplementation"(libs.findLibrary("androidx.compose.ui.test.manifest").get())
            }
        }
    }
}
