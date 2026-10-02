plugins {
    kotlin("jvm") version "2.3.10"
    application
}

group = "com.sever"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "com.sever.MainKt"
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("ai.koog:koog-agents:1.3.0")
    implementation("ai.koog:koog-agents-additions:1.3.0-beta")
}
