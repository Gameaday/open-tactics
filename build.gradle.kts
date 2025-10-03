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
        ignoreFailures.set(false)
    }

    // Detekt configuration
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        source.setFrom(files("src/main/java", "src/main/kotlin"))
    }

    // OWASP Dependency Check configuration
    configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
        // Skip configurations that don't need checking to speed up analysis
        skipConfigurations =
            listOf(
                "lintClassPath",
                "lintChecks",
                "jacocoAgent",
                "jacocoAnt",
                "kotlinCompilerClasspath",
                "kotlinCompilerPluginClasspath",
            )

        // Suppress false positives if needed
        suppressionFile = "${rootProject.projectDir}/config/owasp/suppressions.xml"

        // Fail build only on CVSS score >= 8 (high/critical vulnerabilities)
        failBuildOnCVSS = 8.0f

        // Limit analyzers to speed up scan
        analyzers.apply {
            assemblyEnabled = false
            nugetconfEnabled = false
            nodeEnabled = false
        }
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

// Convenience task for Android lint
tasks.register("lint") {
    dependsOn("app:lint")
    group = "verification"
    description = "Runs Android lint on the default variant"
}

// Convenience task to run all unit tests across all flavors
tasks.register("testAllUnitTests") {
    dependsOn(
        ":app:testDevDebugUnitTest",
        ":app:testProdDebugUnitTest",
    )
    group = "verification"
    description = "Runs unit tests for all product flavors (dev and prod) in debug mode"
}

// Convenience task to assemble all debug variants
tasks.register("assembleAllDebug") {
    dependsOn(
        ":app:assembleDevDebug",
        ":app:assembleProdDebug",
    )
    group = "build"
    description = "Assembles debug builds for all product flavors (dev and prod)"
}

// Convenience task to run all instrumented tests
tasks.register("connectedAllDebugAndroidTest") {
    dependsOn(
        ":app:connectedDevDebugAndroidTest",
        ":app:connectedProdDebugAndroidTest",
    )
    group = "verification"
    description = "Runs instrumented tests for all product flavors (requires connected device/emulator)"
}

// Comprehensive CI verification task
tasks.register("ciVerification") {
    dependsOn(
        "ktlintCheck",
        "detekt",
        "lint",
        "testAllUnitTests",
        "jacocoTestReport",
        "jacocoTestCoverageVerification",
        "assembleAllDebug",
    )
    group = "verification"
    description = "Runs all CI verification checks (quality + tests + build)"
}
