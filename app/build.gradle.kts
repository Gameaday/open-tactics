plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("jacoco")
}

// Version configuration
val versionMajor = 1
val versionMinor = 0
val versionPatch = 0

fun generateVersionCode(): Int =
    providers
        .exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }.standardOutput.asText
        .get()
        .trim()
        .toIntOrNull() ?: 1

fun generateVersionName(): String {
    val versionName =
        if (project.hasProperty("versionName")) {
            project.property("versionName") as String
        } else {
            "$versionMajor.$versionMinor.$versionPatch"
        }

    val gitHash =
        providers
            .exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
            }.standardOutput.asText
            .get()
            .trim()

    val isClean =
        providers
            .exec {
                commandLine("git", "status", "--porcelain")
            }.standardOutput.asText
            .get()
            .trim()
            .isEmpty()

    return if (isClean) {
        versionName
    } else {
        "$versionName-dirty-$gitHash"
    }
}

android {
    namespace = "com.gameaday.opentactics"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.gameaday.opentactics"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()

        versionCode =
            if (project.hasProperty("versionCode")) {
                (project.property("versionCode") as String).toInt()
            } else {
                generateVersionCode()
            }

        versionName = generateVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build config fields for version info
        buildConfigField(
            "String",
            "GIT_HASH",
            "\"${providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }.standardOutput.asText.get().trim()}\"",
        )
        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
    }

    signingConfigs {
        create("release") {
            // Check if signing properties are provided
            if (project.hasProperty("android.injected.signing.store.file")) {
                storeFile = file(project.property("android.injected.signing.store.file") as String)
                storePassword = project.property("android.injected.signing.store.password") as String
                keyAlias = project.property("android.injected.signing.key.alias") as String
                keyPassword = project.property("android.injected.signing.key.password") as String
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            buildConfigField("boolean", "DEBUG_MODE", "true")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = false // Disable minification for now
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            if (project.hasProperty("android.injected.signing.store.file")) {
                signingConfig = signingConfigs.getByName("release")
            }

            buildConfigField("boolean", "DEBUG_MODE", "false")
        }

        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"

            buildConfigField("boolean", "DEBUG_MODE", "true")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.opentactics.com\"")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "false")
        }

        create("prod") {
            dimension = "environment"

            buildConfigField("String", "API_BASE_URL", "\"https://api.opentactics.com\"")
            buildConfigField("boolean", "ENABLE_ANALYTICS", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false

        disable +=
            listOf(
                "TypographyFractions",
                "TypographyQuotes",
            )
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }

        animationsDisabled = true
    }
}

dependencies {
    // AndroidX Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // AndroidX Lifecycle
    implementation(libs.bundles.androidx.lifecycle)

    // AndroidX UI Components
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.preference)

    // AndroidX Navigation
    implementation(libs.bundles.androidx.navigation)

    // Security
    implementation(libs.androidx.security)

    // JSON serialization for save files
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.bundles.testing.unit)

    // Android Testing
    androidTestImplementation(libs.bundles.testing.android)
}

// Task to print version info
tasks.register("printVersionInfo") {
    doLast {
        println("Version Name: ${android.defaultConfig.versionName}")
        println("Version Code: ${android.defaultConfig.versionCode}")
        println("Application ID: ${android.defaultConfig.applicationId}")
    }
}

// JaCoCo configuration for code coverage
jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDevDebugUnitTest", "testProdDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R\$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/databinding/**/*.*",
            "**/generated/**/*.*",
        )

    val debugTree = fileTree("${layout.buildDirectory.asFile.get()}/intermediates/classes/debug")
    debugTree.exclude(fileFilter)
    val kotlinDebugTree = fileTree("${layout.buildDirectory.asFile.get()}/tmp/kotlin-classes/debug")
    kotlinDebugTree.exclude(fileFilter)

    classDirectories.setFrom(debugTree, kotlinDebugTree)
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java", "${project.projectDir}/src/main/kotlin"))
    executionData.setFrom(fileTree(layout.buildDirectory.asFile.get()).include("**/jacoco/*.exec"))
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")

    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% coverage minimum
            }
        }

        rule {
            element = "CLASS"
            limit {
                minimum = "0.70".toBigDecimal() // 70% minimum per class
            }
            excludes =
                listOf(
                    "*.BuildConfig",
                    "*.*Test*",
                    "*.R",
                    "*.R$*",
                    "*.*Activity",
                    "*.*Fragment",
                )
        }
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R\$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/databinding/**/*.*",
            "**/generated/**/*.*",
        )

    val debugTree = fileTree("${layout.buildDirectory.asFile.get()}/intermediates/classes/debug")
    debugTree.exclude(fileFilter)
    val kotlinDebugTree = fileTree("${layout.buildDirectory.asFile.get()}/tmp/kotlin-classes/debug")
    kotlinDebugTree.exclude(fileFilter)

    classDirectories.setFrom(debugTree, kotlinDebugTree)
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java", "${project.projectDir}/src/main/kotlin"))
    executionData.setFrom(fileTree(layout.buildDirectory.asFile.get()).include("**/jacoco/*.exec"))
}
