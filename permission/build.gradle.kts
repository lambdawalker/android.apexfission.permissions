plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.screenshot)
    id("com.vanniktech.maven.publish") version "0.37.0"
}

val projectUrl = "https://github.com/lambdawalker/android.apexfission.permissions"

mavenPublishing {
    coordinates(
        providers.gradleProperty("GROUP").get(),
        providers.gradleProperty("POM_ARTIFACT_ID").get(),
        providers.gradleProperty("releaseVersion").orElse("0.0.0-SNAPSHOT").get(),
    )
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("Apexfission Android Permissions")
        description.set("Jetpack Compose screens, callback helpers, and platform-aware recipes for Android permissions.")
        inceptionYear.set("2026")
        url.set(projectUrl)
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("lambdawalker")
                name.set("David Garcia")
                email.set("lambdawalker@isdavid.com")
                url.set("https://github.com/lambdawalker")
            }
        }
        issueManagement {
            system.set("GitHub Issues")
            url.set("$projectUrl/issues")
        }
        scm {
            url.set(projectUrl)
            connection.set("scm:git:$projectUrl.git")
            developerConnection.set("scm:git:ssh://git@github.com/lambdawalker/android.apexfission.permissions.git")
        }
    }
}

// Development builds need no released version; Central publishing always does.
val explicitReleaseVersion = providers.gradleProperty("releaseVersion")
tasks.configureEach {
    if (name.contains("MavenCentral") && name.startsWith("publish")) {
        val versionToPublish = explicitReleaseVersion.orNull
        doFirst {
            require(versionToPublish?.matches(
                Regex("(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)")
            ) == true) { "Central publication requires -PreleaseVersion=X.Y.Z" }
        }
    }
}

android {
    namespace = "com.apexfission.android.permission"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.keep")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    api(libs.accompanist.permissions)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
    screenshotTestImplementation(platform(libs.androidx.compose.bom))
    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
}
