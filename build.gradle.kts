buildscript {
    dependencies {
        // Match the Kotlin metadata used by the published Demo Common artifact.
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.repo.setup.root)
}
