// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
}

// Both tasks use the same renderer and coordinates as the Maven publication.
// Without an override, keep the version already recorded in IMPORT.md.
val importReleaseVersion = providers.gradleProperty("releaseVersion").orElse("")
val importGroup = providers.gradleProperty("GROUP")
val importArtifact = providers.gradleProperty("POM_ARTIFACT_ID")
listOf("generateImportDocs" to "generate", "verifyImportDocs" to "verify").forEach { (taskName, command) ->
    tasks.register<Exec>(taskName) {
        group = "documentation"
        description = if (command == "generate") "Render IMPORT.md from its template" else "Detect installation documentation drift"
        workingDir(rootDir)
        inputs.file("docs/templates/IMPORT.md.template")
        inputs.file("scripts/release.py")
        inputs.properties(mapOf("releaseVersion" to importReleaseVersion, "groupId" to importGroup, "artifactId" to importArtifact))
        // Always check/render: IMPORT.md is both the default version input and generated output.
        commandLine("python3", "scripts/release.py", command,
            "--version", importReleaseVersion.get(), "--group", importGroup.get(), "--artifact", importArtifact.get())
    }
}
