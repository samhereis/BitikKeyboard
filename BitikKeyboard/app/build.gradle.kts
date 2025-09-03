plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.shoktuk.shoktukkeyboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shoktuk.shoktukkeyboard"
        minSdk = 25
        targetSdk = 36
        versionCode = 10
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22
        targetCompatibility = JavaVersion.VERSION_22
    }
    kotlinOptions {
        jvmTarget = "22"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation(libs.androidx.foundation)

    implementation(libs.ui)
    implementation(libs.androidx.compose.material3.material3)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.appcompat)
    implementation(libs.gson)

    implementation(libs.hilt.android)

    implementation(libs.rhino)

// DataStore)
    implementation(libs.androidx.datastore.preferences)

// Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlin.csv.jvm)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.ui.tooling)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
}