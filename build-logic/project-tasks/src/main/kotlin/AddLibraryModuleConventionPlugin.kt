import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

abstract class AddLibraryModuleConventionPlugin : Plugin<Project> {
        override fun apply(target: Project) {
                val task =
                        target.tasks.register<AddLibraryModuleTask>("addLibraryModule") {
                                group = "project maintenance"
                                description =
                                        "Scaffolds a new Android library module wired " +
                                        "to the project's convention plugins."
                                projectDir = target.layout.projectDirectory.asFile
                                settingsFile =
                                        target.layout.projectDirectory
                                                .file("settings.gradle.kts")
                                                .asFile
                        }

                // Subproject `android {}` blocks aren't configured yet during this apply()
                // call (it runs while the root project itself is being configured), so the
                // app module's namespace can't be read until Gradle has finished
                // configuring the whole project graph - same reasoning as
                // RenameProjectConventionPlugin's use of projectsEvaluated.
                target.gradle.projectsEvaluated { wireAppNamespace(target, task) }
        }
}

private fun wireAppNamespace(
        target: Project,
        task: TaskProvider<AddLibraryModuleTask>,
) {
        val namespaces =
                target.subprojects.mapNotNull {
                        it.extensions
                                .findByType(
                                        ApplicationExtension::class.java,
                                )?.namespace
                }

        task.configure {
                appNamespace = namespaces.singleOrNull()
        }
}
