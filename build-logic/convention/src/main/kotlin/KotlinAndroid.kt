import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk {
            version = release(37) {
                minorApiLevel = 0
            }
        }

        defaultConfig.apply {
            minSdk = 31
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
        }

        sourceSets.all {
            kotlin.directories.clear()
            kotlin.directories.add("src/$name/kotlin")
        }

        testOptions.unitTests.all { it.useJUnitPlatform() }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(17)
    }
}
