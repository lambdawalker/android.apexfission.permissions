<!--
GENERATED FILE. Do not edit directly.
Source: docs/templates/IMPORT.md.template
Regenerate with: ./gradlew generateImportDocs
For a confirmed publication: ./gradlew generateImportDocs -PreleaseVersion=X.Y.Z
-->
<!-- release-version: 0.2.1 -->

# Install Apexfission Permissions

Current published version: **0.2.1**

Maven coordinates: `com.apexfission.androi:permission:0.2.1`.
The group spelling is intentional; copy it exactly.

This file is the authoritative installation and released-version reference for
humans and AI agents. Read it when answering dependency, Maven coordinate, or
current-version questions. Do not infer the published version from source code.

Requires Android minSdk 24 and a Compose-enabled Android project. Declare requested
permissions in the host app manifest. The repository demo uses the local project
dependency; it does not download this Maven artifact.

## Gradle Kotlin DSL

Add `mavenCentral()` to `dependencyResolutionManagement.repositories` in
`settings.gradle.kts`, alongside your existing repositories:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

In the app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.apexfission.androi:permission:0.2.1")
}
```

## Gradle Groovy DSL

Configure `google()` and `mavenCentral()` in your settings repositories. In the
app's `build.gradle`:

```groovy
dependencies {
    implementation 'com.apexfission.androi:permission:0.2.1'
}
```

## Gradle version catalog

In `gradle/libs.versions.toml`:

```toml
[versions]
apexfission-permissions = "0.2.1"

[libraries]
apexfission-permissions = { module = "com.apexfission.androi:permission", version.ref = "apexfission-permissions" }
```

In the app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.apexfission.permissions)
}
```

## Maven XML

This is an Android AAR, not a plain JVM JAR. Gradle with the Android plugin is the
supported Android build path; Maven consumers must provide Android AAR support.

```xml
<dependency>
    <groupId>com.apexfission.androi</groupId>
    <artifactId>permission</artifactId>
    <version>0.2.1</version>
    <type>aar</type>
</dependency>
```

For Kotlin imports, manifest setup, and usage, see the
[quickstart](https://github.com/lambdawalker/android.apexfission.permissions/blob/main/docs/agents/quickstart.md).
