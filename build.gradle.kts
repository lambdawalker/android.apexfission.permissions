plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
}

// A release invocation explicitly selects the permission library; the app is never published.
val jitpackBuild = providers.gradleProperty("jitpackBuild").orElse("false").map { it.toBoolean() }
val releaseModule = providers.gradleProperty("releaseModule").orElse("")
val releaseVersion = providers.gradleProperty("releaseVersion").orElse("0.0.0-SNAPSHOT")
require(releaseModule.get() in listOf("", "permission")) { "Unknown releaseModule" }
if (releaseModule.get().isNotBlank()) {
    require(releaseVersion.get().matches(Regex("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)"))) {
        "Selected module requires a stable releaseVersion"
    }
}
require(!jitpackBuild.get() || releaseModule.get().isNotBlank()) { "JitPack requires one releaseModule" }
subprojects {
    group = rootProject.providers.gradleProperty("GROUP").get()
    version = if (name == releaseModule.get()) releaseVersion.get()
        else "0.0.0-SNAPSHOT"
}
val releaseRepository = providers.environmentVariable("RELEASE_REPOSITORY").orElse("maven-central")
val publishingRepositories = java.util.Properties().apply {
    rootProject.file("publishing/repositories.properties").inputStream().use { load(it) }
}
val releasePublisher = publishingRepositories.getProperty("${releaseRepository.get()}.publisher")
require(releasePublisher in listOf("central", "jitpack")) { "Unknown RELEASE_REPOSITORY; regenerate publishing configuration" }
// Reject accidental multi-module or cross-repository invocations before any reservation is consumed.
gradle.taskGraph.whenReady {
    if (jitpackBuild.get()) {
        allTasks.filter { it is org.gradle.api.publish.maven.tasks.PublishToMavenLocal }.forEach {
            require(it.project.name == releaseModule.get()) { "Only the selected JitPack module may publish: ${it.path}" }
        }
        require(allTasks.none { (it is org.gradle.api.publish.maven.tasks.PublishToMavenRepository && it.repository.name != "verification") || it.name.contains("MavenCentral", ignoreCase = true) }) {
            "JitPack may only publish to MavenLocal"
        }
    }
    allTasks.filter { it.name.contains("MavenCentral", ignoreCase = true) }.forEach {
        val taskPublisher = "central"
        require(taskPublisher == releasePublisher) { "Upload task does not match RELEASE_REPOSITORY: ${it.path}" }
        require(it.project.name == releaseModule.get()) { "Only the selected releaseModule may publish: ${it.path}" }
    }
}
tasks.register<Exec>("verifyPublicationReservation") {
    workingDir(rootDir)
    commandLine("python3", "scripts/module_release.py", "guard", "--module", releaseModule.get(), "--version", releaseVersion.get())
    doFirst {
        require(releaseModule.get().isNotBlank()) { "Set releaseModule to permission" }
        val credentials = listOf("mavenCentralUsername", "mavenCentralPassword", "signingInMemoryKey")
        credentials.forEach {
            require(!providers.gradleProperty(it).orNull.isNullOrBlank()) { "Missing release credential: $it" }
        }
    }
}
listOf("generateImportDocs" to "generate", "verifyImportDocs" to "verify").forEach { (taskName, command) ->
    tasks.register<Exec>(taskName) {
        group = "documentation"
        workingDir(rootDir)
        commandLine("python3", "scripts/module_release.py", command)
    }
}
