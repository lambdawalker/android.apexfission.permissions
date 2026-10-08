<!-- GENERATED FILE. Source: docs/templates/IMPORT.md.template. Run ./gradlew generateImportDocs. -->
# Install Apexfission Permissions

This is the authoritative latest confirmed installation reference. Choose one destination and one dependency syntax. Pending uploads and tags are not proof of availability.

## permission: android.apexfission.permissions

Confirmed version: **0.2.3**. Source: [1ff75b82dfc656a957cee3198195d22708b0490b](https://github.com/lambdawalker/android.apexfission.permissions/commit/1ff75b82dfc656a957cee3198195d22708b0490b).

Choose **one** destination below and **one** dependency syntax. Each destination provides this same release; do not add duplicate dependencies.

### jitpack

Repository: **jitpack**.

#### Gradle Kotlin DSL

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

In the app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.lambdawalker:android.apexfission.permissions:permission~v0.2.3")
}
```

#### Gradle Groovy DSL

In `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

In the app's `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.lambdawalker:android.apexfission.permissions:permission~v0.2.3'
}
```

#### Version catalog

Use the dependency repositories shown above. Add to `gradle/libs.versions.toml`:

```toml
[libraries]
permission = { module = "com.github.lambdawalker:android.apexfission.permissions", version = "permission~v0.2.3" }
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
  <repository>
    <id>confirmed-2</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>com.github.lambdawalker</groupId>
    <artifactId>android.apexfission.permissions</artifactId>
    <version>permission~v0.2.3</version>
    <type>aar</type>
  </dependency>
</dependencies>
```

## Requirements

Android minSdk 24 and a Compose-enabled Android project. Declare requested permissions in the host manifest. The library targets JVM 17; the checked-in Gradle daemon uses Java 25. The demo uses the local project dependency.

See [quickstart](docs/agents/quickstart.md), [release guide](docs/releases.md), and the website's exact-version archive for matching historical guides. Never guess a released version or coordinates.
