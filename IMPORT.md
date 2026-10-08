<!-- GENERATED FILE. Source: docs/templates/IMPORT.md.template. Run ./gradlew generateImportDocs. -->
# Install Apexfission Permissions

This is the authoritative latest confirmed installation reference. Choose one destination and one dependency syntax. Pending uploads and tags are not proof of availability.

## permission

Confirmed legacy Central version: **0.2.1**. The original release source is unknown; no matching versioned guides or JitPack build are claimed.

#### Gradle Kotlin DSL

In `settings.gradle.kts`:

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

#### Gradle Groovy DSL

In `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

In the app's `build.gradle`:

```groovy
dependencies {
    implementation 'com.apexfission.androi:permission:0.2.1'
}
```

#### Version catalog

Use the dependency repositories shown above. Add to `gradle/libs.versions.toml`:

```toml
[libraries]
permission = { module = "com.apexfission.androi:permission", version = "0.2.1" }
```

Then use this instead of the direct dependency in the app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.permission)
}
```

#### Maven

Add these repositories and dependency to `pom.xml`:

```xml
<repositories>
  <repository>
    <id>google</id>
    <url>https://dl.google.com/dl/android/maven2</url>
  </repository>
  <repository>
    <id>central</id>
    <url>https://repo.maven.apache.org/maven2</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>com.apexfission.androi</groupId>
    <artifactId>permission</artifactId>
    <version>0.2.1</version>
    <type>aar</type>
  </dependency>
</dependencies>
```

## Requirements

Android minSdk 24 and a Compose-enabled Android project. Declare requested permissions in the host manifest. The library targets JVM 17; the checked-in Gradle daemon uses Java 25. The demo uses the local project dependency.

See [quickstart](docs/agents/quickstart.md), [release guide](docs/releases.md), and the website's exact-version archive for matching historical guides. Never guess a released version or coordinates.
