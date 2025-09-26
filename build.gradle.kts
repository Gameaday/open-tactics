// Top-level build file for a standalone Kotlin project to demonstrate game mechanics
plugins {
    kotlin("jvm") version "2.1.0"
    application
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.gameaday.opentactics.MainKt")
}

tasks.test {
    useJUnitPlatform()
}