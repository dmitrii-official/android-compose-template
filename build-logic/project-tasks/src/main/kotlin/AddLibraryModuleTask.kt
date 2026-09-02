import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

private val validModuleName = Regex("[a-zA-Z][a-zA-Z0-9-]*")

abstract class AddLibraryModuleTask : DefaultTask() {
        @get:Internal
        var projectDir: File? = null

        @get:Internal
        var settingsFile: File? = null

        @get:Internal
        var appNamespace: String? = null

        @get:Input
        @get:Option(
                option = "name",
                description =
                        "New module's directory / Gradle project name, e.g. 'network'.",
        )
        abstract val moduleName: Property<String>

        @get:Input
        @get:Optional
        @get:Option(
                option = "namespace",
                description =
                        "New module's namespace / Kotlin package. Defaults to " +
                                "'<app namespace>.<module name>'.",
        )
        abstract val newNamespace: Property<String>

        @TaskAction
        fun addModule() {
                val root =
                        projectDir ?: throw GradleException(
                                "addLibraryModule: project directory was not resolved.",
                        )
                val settings =
                        settingsFile ?: throw GradleException(
                                "addLibraryModule: settings.gradle.kts was not resolved.",
                        )

                val name = moduleName.getOrElse("").trim()
                require(name.isNotBlank()) { "--name must not be blank" }
                require(validModuleName.matches(name)) {
                        "--name must match ${validModuleName.pattern} (got '$name')"
                }

                val gradlePath = ":$name"
                require(!settings.readText().contains("include(\"$gradlePath\")")) {
                        "'$gradlePath' is already included in settings.gradle.kts"
                }

                val moduleDir = File(root, name)
                require(!moduleDir.exists()) { "'${moduleDir.path}' already exists" }

                val namespace =
                        newNamespace.orNull
                                ?.trim()
                                ?.takeIf { it.isNotBlank() }
                                ?: defaultNamespace(name)
                require(namespace.isNotBlank()) { "--namespace must not be blank" }

                createModule(moduleDir, namespace)
                appendModuleInclude(settings, gradlePath)

                logger.lifecycle("Module:    {}", gradlePath)
                logger.lifecycle("Namespace: {}", namespace)
                logger.lifecycle("Location:  {}", moduleDir.path)
                logger.lifecycle(
                        "Added. Re-sync / re-run Gradle to pick up the new module.",
                )
        }

        private fun defaultNamespace(name: String): String {
                val base =
                        appNamespace?.takeIf { it.isNotBlank() } ?: throw GradleException(
                                "Couldn't determine a default namespace (need exactly " +
                                        "one com.android.application module); pass " +
                                        "--namespace explicitly.",
                        )
                val segment = name.lowercase().filter { it.isLetterOrDigit() }
                require(segment.isNotBlank()) {
                        "--name must contain at least one letter or digit to derive " +
                                "a namespace"
                }
                return "$base.$segment"
        }
}

private fun createModule(
        moduleDir: File,
        namespace: String,
) {
        val packagePath = namespace.replace('.', File.separatorChar)
        val mainDir = File(moduleDir, "src/main/kotlin/$packagePath")
        val testDir = File(moduleDir, "src/test/kotlin/$packagePath")
        mainDir.mkdirs()
        testDir.mkdirs()

        File(moduleDir, ".gitignore").writeText("/build\n")

        File(moduleDir, "build.gradle.kts").writeText(
                """
                plugins {
                        alias(libs.plugins.convention.android.library)
                }

                android {
                        namespace = "$namespace"
                }
                """.trimIndent() + "\n",
        )

        File(mainDir, ".gitkeep").writeText("")

        File(testDir, "ExampleUnitTest.kt").writeText(
                """
                package $namespace

                import org.junit.jupiter.api.Assertions.assertEquals
                import org.junit.jupiter.api.Test

                class ExampleUnitTest {
                        @Test
                        fun addition_isCorrect() {
                                assertEquals(4, 2 + 2)
                        }
                }
                """.trimIndent() + "\n",
        )
}

private fun appendModuleInclude(
        settingsFile: File,
        gradlePath: String,
) {
        val original = settingsFile.readText()
        val updated = original.trimEnd('\n') + "\ninclude(\"$gradlePath\")\n"
        settingsFile.writeText(updated)
}
