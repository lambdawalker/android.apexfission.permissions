plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.screenshot)
    id("com.vanniktech.maven.publish") version "0.37.0"
}

mavenPublishing {
    coordinates(
        "com.apexfission.android.permission",
        "core",
        providers.gradleProperty("permissionVersion").orElse("0.1.0").get(),
    )
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("Apexfission Permissions")
        description.set("Customizable Jetpack Compose screens and callback helpers for Android runtime permissions.")
        inceptionYear.set("2026")
        url.set("https://github.com/lambdawalker/android.apexfission.permissions")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
        scm {
            url.set("https://github.com/lambdawalker/android.apexfission.permissions")
            connection.set("scm:git:https://github.com/lambdawalker/android.apexfission.permissions.git")
            developerConnection.set("scm:git:ssh://git@github.com/lambdawalker/android.apexfission.permissions.git")
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
    implementation(libs.androidx.lifecycle.viewmodel.compose)
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
