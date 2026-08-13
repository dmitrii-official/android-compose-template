import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class GitHooksConventionPlugin : Plugin<Project> {
        override fun apply(target: Project) {
                if (!target.file(".git").exists()) return

                runCatching {
                        target.providers
                                .exec {
                                        workingDir = target.projectDir
                                        commandLine(
                                                "git",
                                                "config",
                                                "core.hooksPath",
                                                ".githooks",
                                        )
                                }.result
                                .get()
                }
        }
}
