import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureJUnit5Testing() {
    dependencies {
        "testImplementation"(platform(libs.findLibrary("junit.bom").get()))
        "testImplementation"(libs.findLibrary("junit.jupiter").get())
        "testImplementation"(libs.findLibrary("mockk").get())
        "testImplementation"(libs.findLibrary("turbine").get())
        "testImplementation"(libs.findLibrary("kotlinxCoroutinesTest").get())
        "testImplementation"(libs.findLibrary("robolectric").get())
        "testImplementation"(libs.findLibrary("androidx.test.core.ktx").get())
        "testImplementation"(libs.findLibrary("androidx.junit").get())
        "testRuntimeOnly"(libs.findLibrary("junit.vintage.engine").get())
        "testRuntimeOnly"(libs.findLibrary("junit.launcher").get())

        "androidTestImplementation"(platform(libs.findLibrary("junit.bom").get()))
        "androidTestImplementation"(libs.findLibrary("junit.jupiter").get())
        "androidTestImplementation"(libs.findLibrary("android.junit5.core").get())
        "androidTestImplementation"(libs.findLibrary("androidx.test.runner").get())
        "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
        "androidTestImplementation"(libs.findLibrary("mockk.android").get())
        "androidTestRuntimeOnly"(libs.findLibrary("android.junit5.runner").get())
    }
}
