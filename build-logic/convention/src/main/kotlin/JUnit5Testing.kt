import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureJUnit5Testing() {
        val junitBomVersion = libs.findLibrary("junit.bom").get()
        val junitJupiter = libs.findLibrary("junit.jupiter").get()
        val mockk = libs.findLibrary("mockk").get()
        val turbine = libs.findLibrary("turbine").get()
        val kotlinxCoroutinesTest = libs.findLibrary("kotlinxCoroutinesTest").get()
        val robolectric = libs.findLibrary("robolectric").get()
        val androidxTestCoreKtx = libs.findLibrary("androidx.test.core.ktx").get()
        val androidxJunit = libs.findLibrary("androidx.junit").get()
        val junitVintageEngine = libs.findLibrary("junit.vintage.engine").get()
        val junitLauncher = libs.findLibrary("junit.launcher").get()
        val androidJunit5Core = libs.findLibrary("android.junit5.core").get()
        val androidxTestRunner = libs.findLibrary("androidx.test.runner").get()
        val mockkAndroid = libs.findLibrary("mockk.android").get()
        val androidJunit5Runner = libs.findLibrary("android.junit5.runner").get()

        dependencies {
                "testImplementation"(platform(junitBomVersion))
                "testImplementation"(junitJupiter)
                "testImplementation"(mockk)
                "testImplementation"(turbine)
                "testImplementation"(kotlinxCoroutinesTest)
                "testImplementation"(robolectric)
                "testImplementation"(androidxTestCoreKtx)
                "testImplementation"(androidxJunit)
                "testRuntimeOnly"(junitVintageEngine)
                "testRuntimeOnly"(junitLauncher)

                "androidTestImplementation"(platform(junitBomVersion))
                "androidTestImplementation"(junitJupiter)
                "androidTestImplementation"(androidJunit5Core)
                "androidTestImplementation"(androidxTestRunner)
                "androidTestImplementation"(androidxJunit)
                "androidTestImplementation"(mockkAndroid)
                "androidTestRuntimeOnly"(androidJunit5Runner)
        }
}
