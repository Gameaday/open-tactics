// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.7" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("com.github.ben-manes.versions") version "0.51.0" apply false
    id("org.owasp.dependencycheck") version "10.0.4" apply false
    kotlin("jvm") version "2.1.0" apply false
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
        ignoreFailures.set(false)
    }
}

// Task to run all quality checks
tasks.register("qualityCheck") {
    dependsOn(
        "ktlintCheck",
        "detekt", 
        subprojects.map { "${it.name}:test" }
    )
}