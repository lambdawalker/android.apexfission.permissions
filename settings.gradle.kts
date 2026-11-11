pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id in setOf(
                    "com.apexfission.android-repo-setup",
                    "com.apexfission.android-repo-setup.root",
                )) {
                requested.version?.let { version ->
                    useModule("com.github.lambdawalker:gradle.android-repo-setup:$version")
                }
            }
        }
    }
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io") {
            content { includeModule("com.github.lambdawalker", "gradle.android-repo-setup") }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android.apexfission.permissions"
include(":app")
include(":permission")
