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

fun generateVersionCode(): Int {
    val commitCount = "git rev-list --count HEAD".runCommand()?.toIntOrNull() ?: 1
    return commitCount
}

fun generateVersionName(): String {
    val versionName = if (project.hasProperty("versionName")) {
        project.property("versionName") as String
    } else {
        "$versionMajor.$versionMinor.$versionPatch"
    }
    
    val gitHash = "git rev-parse --short HEAD".runCommand() ?: "unknown"
    val isClean = "git status --porcelain".runCommand()?.isEmpty() ?: true
    
    return if (isClean) {
        versionName
    } else {
        "$versionName-dirty-$gitHash"
    }
}

fun String.runCommand(): String? {
    return try {
        ProcessBuilder(*split(" ").toTypedArray())
            .directory(projectDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        null
    }
}

android {
    namespace = "com.gameaday.opentactics"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gameaday.opentactics"
        minSdk = 24
        targetSdk = 35
        
        versionCode = if (project.hasProperty("versionCode")) {
            (project.property("versionCode") as String).toInt()
        } else {
            generateVersionCode()
        }
        
        versionName = generateVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Build config fields for version info
        buildConfigField("String", "GIT_HASH", "\"${("git rev-parse --short HEAD".runCommand() ?: "unknown")}\"")
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
            isMinifyEnabled = true
            isShrinkResources = true
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    kotlinOptions {
        jvmTarget = "21"
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false
        
        baseline = file("lint-baseline.xml")
        
        disable += listOf(
            "TypographyFractions",
            "TypographyQuotes"
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
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    
    // JSON serialization for save files
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Shared preferences for save/load
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("org.robolectric:robolectric:4.11.1")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
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
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**/*.*",
        "**/generated/**/*.*"
    )
    
    val debugTree = fileTree("${buildDir}/intermediates/classes/debug")
    debugTree.exclude(fileFilter)
    val kotlinDebugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug")
    kotlinDebugTree.exclude(fileFilter)
    
    classDirectories.setFrom(debugTree, kotlinDebugTree)
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java", "${project.projectDir}/src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir).include("**/jacoco/*.exec"))
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()  // 80% coverage minimum
            }
        }
        
        rule {
            element = "CLASS"
            limit {
                minimum = "0.70".toBigDecimal()  // 70% minimum per class
            }
            excludes = listOf(
                "*.BuildConfig",
                "*.*Test*",
                "*.R",
                "*.R$*",
                "*.*Activity",
                "*.*Fragment"
            )
        }
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/**/*.*",
        "**/generated/**/*.*"
    )
    
    val debugTree = fileTree("${buildDir}/intermediates/classes/debug")
    debugTree.exclude(fileFilter)
    val kotlinDebugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug")
    kotlinDebugTree.exclude(fileFilter)
    
    classDirectories.setFrom(debugTree, kotlinDebugTree)
    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java", "${project.projectDir}/src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir).include("**/jacoco/*.exec"))
}