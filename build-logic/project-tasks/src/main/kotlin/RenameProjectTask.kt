import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.nio.file.Files

data class AppModuleInfo(
        val path: String,
        val buildFile: File,
        val namespace: String,
        val applicationId: String,
)

abstract class RenameProjectTask : DefaultTask() {
        @get:Internal
        var projectDir: File? = null

        @get:Internal
        var currentProjectName = ""

        @get:Internal
        var sourceRootDirs: List<File> = emptyList()

        @get:Internal
        var appModules: List<AppModuleInfo> = emptyList()

        @get:Input
        @get:Optional
        @get:Option(option = "namespace", description = "New namespace / Kotlin package.")
        abstract val newNamespace: Property<String>

        @get:Input
        @get:Optional
        @get:Option(
                option = "application-id",
                description = "New applicationId (defaults to --namespace).",
        )
        abstract val newApplicationId: Property<String>

        @get:Input
        @get:Optional
        @get:Option(option = "app-name", description = "New app / project name.")
        abstract val newAppName: Property<String>

        @get:Input
        @get:Optional
        @get:Option(
                option = "app-module",
                description =
                        "Project path of the application module to rename, e.g. :app. " +
                                "Only needed when there's more than one.",
        )
        abstract val appModulePath: Property<String>

        @TaskAction
        fun rename() {
                if (!newNamespace.isPresent && !newApplicationId.isPresent &&
                        !newAppName.isPresent
                ) {
                        throw GradleException(
                                "Usage: --namespace=<id> --application-id=<id> --app-name=<name>",
                        )
                }

                val root =
                        projectDir ?: throw GradleException(
                                "renameProject: project directory was not resolved.",
                        )
                val appModule = selectAppModule()
                val oldNamespace = appModule.namespace
                val oldApplicationId = appModule.applicationId
                val oldProjectName = currentProjectName

                val namespace = newNamespace.getOrElse(oldNamespace)
                val appName = newAppName.getOrElse(oldProjectName)
                val applicationId =
                        when {
                                newApplicationId.isPresent -> newApplicationId.get()
                                oldNamespace == oldApplicationId -> namespace
                                else -> oldApplicationId
                        }

                require(namespace.isNotBlank()) { "--namespace must not be blank" }
                require(
                        applicationId.isNotBlank(),
                ) { "--application-id must not be blank" }
                require(appName.isNotBlank()) { "--app-name must not be blank" }

                logger.lifecycle("Module:        {}", appModule.path)
                logger.lifecycle("Namespace:     {} -> {}", oldNamespace, namespace)
                logger.lifecycle(
                        "ApplicationId: {} -> {}",
                        oldApplicationId,
                        applicationId,
                )
                logger.lifecycle("Name:          {} -> {}", oldProjectName, appName)

                if (namespace != oldNamespace || appName != oldProjectName) {
                        replaceInFiles(
                                root,
                                oldNamespace,
                                namespace,
                                oldProjectName,
                                appName,
                        )
                }
                moveSourceDirs(sourceRootDirs, oldNamespace, namespace)
                patchApplicationId(appModule.buildFile, applicationId)

                logger.lifecycle("Renamed. Review the diff, then re-init git history:")
                logger.lifecycle("rm -rf .git && git init && git add -A && git commit")
        }

        private fun selectAppModule(): AppModuleInfo {
                if (appModulePath.isPresent) {
                        val path = appModulePath.get()
                        return appModules.find { it.path == path }
                                ?: throw GradleException(
                                        "No com.android.application module at '$path'. " +
                                                candidateModulesMessage(),
                                )
                }
                return appModules.singleOrNull()
                        ?: throw GradleException(
                                "Found ${appModules.size} app module(s); pass " +
                                        "--app-module=<path> to pick one. " +
                                        candidateModulesMessage(),
                        )
        }

        private fun candidateModulesMessage(): String =
                if (appModules.isEmpty()) {
                        "No com.android.application modules were found in this project."
                } else {
                        "Candidates: ${appModules.joinToString { it.path }}"
                }
}

private fun patchApplicationId(
        appBuildFile: File,
        applicationId: String,
) {
        val original = appBuildFile.readText()
        val updated =
                Regex("applicationId\\s*=\\s*\"[^\"]*\"")
                        .replace(original) { "applicationId = \"$applicationId\"" }
        if (updated != original) {
                appBuildFile.writeText(updated)
        }
}

private val excludedDirNames = setOf(".git", ".gradle", "build")

private fun replaceInFiles(
        root: File,
        oldNamespace: String,
        newNamespace: String,
        oldProjectName: String,
        newAppName: String,
) {
        val safeAppName = newAppName.filter { it.isLetterOrDigit() }

        root
                .walkTopDown()
                .onEnter { it.name !in excludedDirNames }
                .filter { it.isFile && !looksBinary(it) }
                .forEach { file ->
                        val original = file.readText()
                        val updated =
                                original
                                        .replace(oldNamespace, newNamespace)
                                        .replace(
                                                "Theme.$oldProjectName",
                                                "Theme.$safeAppName",
                                        ).replace(oldProjectName, newAppName)
                        if (updated != original) {
                                file.writeText(updated)
                        }
                }
}

private fun looksBinary(file: File): Boolean {
        val header = file.inputStream().use { it.readNBytes(8000) }
        return header.any { it == 0.toByte() }
}

private fun moveSourceDirs(
        sourceRootDirs: List<File>,
        oldNamespace: String,
        newNamespace: String,
) {
        if (oldNamespace == newNamespace) return

        val oldPath = oldNamespace.replace('.', File.separatorChar)
        val newPath = newNamespace.replace('.', File.separatorChar)

        for (sourceRoot in sourceRootDirs) {
                val source = File(sourceRoot, oldPath)
                if (!source.isDirectory) continue

                val destination = File(sourceRoot, newPath)
                destination.parentFile.mkdirs()
                Files.move(source.toPath(), destination.toPath())
                deleteEmptyAncestors(source.parentFile, boundary = sourceRoot)
        }
}

private fun deleteEmptyAncestors(
        start: File,
        boundary: File,
) {
        var dir = start
        while (dir != boundary && dir.isDirectory && dir.list().isNullOrEmpty()) {
                val parent = dir.parentFile
                dir.delete()
                dir = parent
        }
}
