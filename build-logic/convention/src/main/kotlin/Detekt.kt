import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

internal fun Project.configureDetekt() {
        apply(plugin = "io.gitlab.arturbosch.detekt")

        extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                parallel = true
        }
}
