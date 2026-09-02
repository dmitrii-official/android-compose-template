import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

abstract class RenameProjectConventionPlugin : Plugin<Project> {
        override fun apply(target: Project) {
                val task =
                        target.tasks.register<RenameProjectTask>("renameProject") {
                                group = "project maintenance"
                                description =
                                        "Renames the template's package and app name."
                                projectDir = target.layout.projectDirectory.asFile
                                currentProjectName = target.rootProject.name
                        }

                // Subproject `android {}` blocks aren't configured yet during this apply() call
                // (it runs while the root project itself is being configured), so namespace,
                // applicationId, and sourceSets can't be read from other modules until Gradle
                // has finished configuring the whole project graph - hence projectsEvaluated
                // instead of doing this above.
                target.gradle.projectsEvaluated { wireAppModules(target, task) }
        }
}

private fun wireAppModules(
        target: Project,
        task: TaskProvider<RenameProjectTask>,
) {
        val appModules = target.subprojects.mapNotNull { it.appModuleInfo() }
        val sourceDirs = target.subprojects.flatMap { it.androidSourceRootDirs() }

        task.configure {
                sourceRootDirs = sourceDirs
                this.appModules = appModules
        }
}

private fun Project.appModuleInfo(): AppModuleInfo? {
        val extension =
                extensions.findByType(ApplicationExtension::class.java) ?: return null
        val namespace = extension.namespace ?: return null
        val applicationId = extension.defaultConfig.applicationId ?: return null
        return AppModuleInfo(path, file("build.gradle.kts"), namespace, applicationId)
}

private fun Project.file(relativePath: String): File =
        layout.projectDirectory.file(relativePath).asFile

private fun Project.androidSourceSets(): List<AndroidSourceSet> {
        extensions.findByType(ApplicationExtension::class.java)?.let {
                return it.sourceSets.toList()
        }
        extensions.findByType(LibraryExtension::class.java)?.let {
                return it.sourceSets.toList()
        }
        return emptyList()
}

private fun Project.androidSourceRootDirs(): List<File> =
        androidSourceSets().flatMap { sourceSet ->
                (sourceSet.kotlin.directories + sourceSet.java.directories)
                        .map { layout.projectDirectory.dir(it).asFile }
        }
