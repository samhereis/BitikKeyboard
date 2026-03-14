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
        versionCode = 50
        versionName = "3.3.7"

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
    implementation(libs.colorpicker.compose)

    // Core & App Compat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.material)

    // Compose Bill of Materials (BOM)
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)

    // Compose Material 3
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Compose Navigation & ViewModel
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Other Dependencies
    implementation(libs.flexbox)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.wrapper.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlin.csv.jvm)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // Testing
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material3)
}