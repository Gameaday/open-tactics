plugins {
    kotlin("jvm")
    application
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