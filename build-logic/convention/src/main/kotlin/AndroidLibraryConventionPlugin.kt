import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

abstract class AndroidLibraryConventionPlugin : Plugin<Project> {
        override fun apply(target: Project) {
                with(target) {
                        apply(plugin = "com.android.library")
                        apply(plugin = "de.mannodermaus.android-junit5")

                        extensions.configure<LibraryExtension> {
                                configureKotlinAndroid(this)

                                lint {
                                        warningsAsErrors = true
                                        // Dependency freshness is handled by Renovate, not by lint.
                                        disable += "GradleDependency"
                                }
                        }

                        configureJUnit5Testing()
                        configureSpotless()
                        configureDetekt()
                }
        }
}
