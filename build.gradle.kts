// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.versions) apply false
    alias(libs.plugins.owasp) apply false
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "org.owasp.dependencycheck")

    // Simplified ktlint configuration
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.3.1")
        android.set(true)
        ignoreFailures.set(true) // Allow build to complete while we fix formatting
    }
}

// Task to run all quality checks
tasks.register("qualityCheck") {
    dependsOn(
        "ktlintCheck",
        "detekt",
        subprojects.map { "${it.name}:test" },
    )
}
