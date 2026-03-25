plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("wallettask.jks")
            storePassword = "wallettask"
            keyAlias = "wallettask"
            keyPassword = "wallettask"
        }
        create("release") {
            storeFile = file("wallettask.jks")
            storePassword = "wallettask"
            keyAlias = "wallettask"
            keyPassword = "wallettask"
        }
    }
    namespace = "com.app.wallettask"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.wallettask"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // QR Code
    implementation(libs.zxing.android.embedded)
    implementation(libs.zxing.core)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Lifecycle
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)

    implementation(libs.play.services.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}