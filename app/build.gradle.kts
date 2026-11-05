plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.rns.evacardspack"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.rns.evacardspack"
        minSdk = 26
        targetSdk = 37
        versionCode = 7
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "card"
    productFlavors {
        create("weather") {
            dimension = "card"
            applicationId = "io.rns.evacardspack.weather"
            versionNameSuffix = "-weather"
        }
        create("clock") {
            dimension = "card"
            applicationId = "io.rns.evacardspack.clock"
            versionNameSuffix = "-clock"
        }
        create("trending") {
            dimension = "card"
            applicationId = "io.rns.evacardspack.trending"
            versionNameSuffix = "-trending"
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
