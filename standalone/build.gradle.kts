plugins {
    kotlin("jvm")
    application
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("com.gameaday.opentactics.MainKt")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}