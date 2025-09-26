// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.1" apply false
    id("org.jetbrains.dokka") version "1.9.10" apply false
    id("com.github.ben-manes.versions") version "0.50.0" apply false
    id("org.owasp.dependencycheck") version "9.0.7" apply false
    kotlin("jvm") version "2.1.0" apply false
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "org.owasp.dependencycheck")
    
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("0.50.0")
        android.set(true)
        ignoreFailures.set(false)
        reporters.set(setOf(
            org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN,
            org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE,
            org.jlleitschuh.gradle.ktlint.reporter.ReporterType.SARIF
        ))
        
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }
    
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        baseline = file("$rootDir/config/detekt/baseline.xml")
        
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(true)
        }
    }
    
    configure<com.github.benmanes.gradle.versions.VersionsExtension> {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
    }
    
    configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
        format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL
        suppressionFile = "$rootDir/config/owasp/suppressions.xml"
        failBuildOnCVSS = 7.0f
        analyzers.apply {
            assemblyEnabled = false
            nuspecEnabled = false
            nugetconfEnabled = false
            centralEnabled = false
        }
    }
}

// Task to check for dependency updates across all projects
tasks.register("checkDependencyUpdates") {
    dependsOn(subprojects.map { "${it.name}:dependencyUpdates" })
}

// Task to run all quality checks
tasks.register("qualityCheck") {
    dependsOn(
        "ktlintCheck",
        "detekt", 
        subprojects.map { "${it.name}:test" },
        subprojects.map { "${it.name}:lint" }.filter { 
            subprojects.any { project -> 
                project.plugins.hasPlugin("com.android.application") || 
                project.plugins.hasPlugin("com.android.library") 
            } 
        }
    )
}