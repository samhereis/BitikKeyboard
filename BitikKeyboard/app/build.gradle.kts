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
        versionCode = 23
        versionName = "3.1.8"

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
    // ── Core & AppCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ── Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // ── Compose UI
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.foundation)

    // Tooling: preview at runtime, inspector only in debug
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // ── Material 3
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.material)

    // ── Navigation & ViewModel
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ── Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // ── Other
    implementation(libs.flexbox)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.wrapper.android)            // your QuickJS wrapper alias
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlin.csv.jvm)
    implementation(libs.androidx.room.ktx)

    // ── Testing (scoped properly)
    testImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.monitor)
}