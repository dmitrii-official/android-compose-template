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

                                lint {
                                        warningsAsErrors = true
                                        // Dependency freshness is handled by Renovate, not by lint.
                                        disable += "GradleDependency"
                                }
                        }

                        configureJUnit5Testing()
                        configureSpotless()
                        configureDetekt()
                        configureDependencies()
                }
        }

        private fun Project.configureDependencies() {
                val appcompat = libs.findLibrary("androidx.appcompat").get()
                val coreKtx = libs.findLibrary("androidx.core.ktx").get()
                val material = libs.findLibrary("material").get()

                val composeBom = libs.findLibrary("androidx.compose.bom").get()
                val activityCompose = libs.findLibrary("androidx.activity.compose").get()

                val composeMaterial3 =
                        libs
                                .findLibrary("androidx.compose.material3")
                                .get()
                val composeUi = libs.findLibrary("androidx.compose.ui").get()
                val composeUiGraphics =
                        libs
                                .findLibrary("androidx.compose.ui.graphics")
                                .get()
                val composeUiToolingPreview =
                        libs
                                .findLibrary("androidx.compose.ui.tooling.preview")
                                .get()
                val lifecycleRuntimeKtx =
                        libs
                                .findLibrary("androidx.lifecycle.runtime.ktx")
                                .get()

                val koinBom = libs.findLibrary("koin.bom").get()
                val koinAndroid = libs.findLibrary("koin.android").get()
                val koinAndroidxCompose = libs.findLibrary("koin.androidx.compose").get()
                val kotlinxCoroutinesAndroid =
                        libs
                                .findLibrary("kotlinx.coroutines.android")
                                .get()
                val navigationCompose =
                        libs
                                .findLibrary("androidx.navigation.compose")
                                .get()
                val composeUiTextGoogleFonts =
                        libs
                                .findLibrary("androidx.compose.ui.text.google.fonts")
                                .get()
                val timber = libs.findLibrary("timber").get()
                val composeMaterialIconsExtended =
                        libs
                                .findLibrary("androidx.compose.material.icons.extended")
                                .get()

                val koinTestJunit5 = libs.findLibrary("koin.test.junit5").get()

                val composeUiTestJunit4 =
                        libs
                                .findLibrary("androidx.compose.ui.test.junit4")
                                .get()

                val composeUiTooling =
                        libs
                                .findLibrary("androidx.compose.ui.tooling")
                                .get()
                val composeUiTestManifest =
                        libs
                                .findLibrary("androidx.compose.ui.test.manifest")
                                .get()

                dependencies {
                        "implementation"(appcompat)
                        "implementation"(coreKtx)
                        "implementation"(material)

                        "implementation"(platform(composeBom))
                        "implementation"(activityCompose)

                        "implementation"(composeMaterial3)
                        "implementation"(composeUi)
                        "implementation"(composeUiGraphics)
                        "implementation"(composeUiToolingPreview)
                        "implementation"(lifecycleRuntimeKtx)

                        "implementation"(platform(koinBom))
                        "implementation"(koinAndroid)
                        "implementation"(koinAndroidxCompose)
                        "implementation"(kotlinxCoroutinesAndroid)
                        "implementation"(navigationCompose)
                        "implementation"(composeUiTextGoogleFonts)
                        "implementation"(timber)
                        "implementation"(composeMaterialIconsExtended)

                        "testImplementation"(koinTestJunit5)

                        "androidTestImplementation"(platform(composeBom))
                        "androidTestImplementation"(composeUiTestJunit4)

                        "debugImplementation"(composeUiTooling)
                        "debugImplementation"(composeUiTestManifest)
                }
        }
}
