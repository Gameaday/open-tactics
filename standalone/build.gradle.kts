plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.ktlint)
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
