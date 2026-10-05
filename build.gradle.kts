import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerLogsContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    application
    id("com.bmuschko.docker-remote-api") version "9.4.0"
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
    implementation("ai.koog:agents-features-event-handler:1.3.0")
    implementation("ai.koog:skills:1.3.0-beta")
    implementation("org.yaml:snakeyaml:2.3")
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")
}

val imageName = "rainy-agent:${project.version}"

// Task prompt passed as the container command, e.g. ./gradlew devRun -Pprompt="scan example.com"
val promptCmd = providers.gradleProperty("prompt").map { listOf(it) }.orElse(emptyList())

// Host logs directory bind-mounted into the containers so per-run jsonl logs persist.
val logsDir = project.file("logs").apply { mkdirs() }.absolutePath

// .env values (API key, model) injected into the containers the docker tasks run.
val dotenv: Map<String, String> = file(".env").takeIf { it.isFile }
    ?.readLines()
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
    ?.associate { it.substringBefore("=").trim() to it.substringAfter("=").trim().trim('"') }
    ?: emptyMap()

val dockerContext by tasks.registering(Sync::class) {
    group = "docker"
    description = "Assembles the minimal Docker build context."
    from(tasks.installDist) { into("app") }
    from("skills") { into("skills") }
    from("Dockerfile")
    into(layout.buildDirectory.dir("docker"))
}

val buildImage by tasks.registering(DockerBuildImage::class) {
    group = "docker"
    description = "Builds the agent container image with its toolchain."
    dependsOn(dockerContext)
    inputDir.set(layout.buildDirectory.dir("docker"))
    images.add(imageName)
}

val createContainer by tasks.registering(DockerCreateContainer::class) {
    group = "docker"
    description = "Creates a container from the agent image."
    dependsOn(buildImage)
    targetImageId(imageName)
    cmd.set(promptCmd)
    envVars.set(dotenv)
    hostConfig.binds.set(mapOf(logsDir to "/app/logs"))
    hostConfig.capAdd.set(listOf("NET_RAW", "NET_ADMIN"))
    hostConfig.autoRemove.set(true)
}

val logsContainer by tasks.registering(DockerLogsContainer::class) {
    targetContainerId(createContainer.flatMap { it.containerId })
    follow.set(true)
    tailAll.set(true)
}

tasks.register<DockerStartContainer>("runImage") {
    group = "docker"
    description = "Runs the agent container."
    dependsOn(createContainer)
    finalizedBy(logsContainer)
    targetContainerId(createContainer.flatMap { it.containerId })
}

// Fast dev loop: reuse the prebuilt tool image, bind-mount the freshly-built dist (no image rebuild).
// Run `buildImage` once first; afterwards only `installDist` reruns on code changes.
val devCreateContainer by tasks.registering(DockerCreateContainer::class) {
    group = "docker"
    description = "Creates a dev container with the freshly-built dist bind-mounted."
    dependsOn(tasks.installDist)
    targetImageId(imageName)
    cmd.set(promptCmd)
    envVars.set(dotenv)
    hostConfig.binds.set(
        mapOf(
            layout.buildDirectory.dir("install/rainy").get().asFile.absolutePath to "/app",
            logsDir to "/app/logs",
        ),
    )
    hostConfig.capAdd.set(listOf("NET_RAW", "NET_ADMIN"))
    hostConfig.autoRemove.set(true)
}

val devLogsContainer by tasks.registering(DockerLogsContainer::class) {
    targetContainerId(devCreateContainer.flatMap { it.containerId })
    follow.set(true)
    tailAll.set(true)
}

tasks.register<DockerStartContainer>("devRun") {
    group = "docker"
    description = "Runs the agent in the prebuilt image with the current dist mounted (no image rebuild)."
    dependsOn(devCreateContainer)
    finalizedBy(devLogsContainer)
    targetContainerId(devCreateContainer.flatMap { it.containerId })
}

tasks.named("build") {
    dependsOn(buildImage)
}
