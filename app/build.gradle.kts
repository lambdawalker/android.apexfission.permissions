plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.apexfission.android.permissions"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.apexfission.android.permissions"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        suites {
            create("journeysTest") {
                assets {
                }
                targets {
                    create("default") {
                    }
                }
                useJunitEngine {
                    inputs += listOf(com.android.build.api.dsl.AgpTestSuiteInputParameters.TESTED_APKS)
                    includeEngines += listOf("journeys-test-engine")
                    enginesDependencies(libs.junit.platform.launcher)
                    enginesDependencies(libs.junit.platform.engine)
                    enginesDependencies(libs.journeys.junit.engine)
                }
                targetVariants += listOf("debug")
            }
        }
    }
}

dependencies {
    implementation(project(":permission"))
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
