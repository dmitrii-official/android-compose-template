import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

internal fun Project.configureSpotless() {
        apply(plugin = "com.diffplug.spotless")

        val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion

        extensions.configure<SpotlessExtension> {
                kotlin {
                        target("src/**/*.kt")
                        targetExclude("**/build/**/*.kt")
                        ktlint(ktlintVersion)
                }
                kotlinGradle {
                        target("*.gradle.kts")
                        ktlint(ktlintVersion)
                }
        }
}
