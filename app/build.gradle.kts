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
        versionCode = 6
        versionName = "0.2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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