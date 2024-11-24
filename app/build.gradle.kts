plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.crashlytics.gradle)
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("com.google.gms.google-services")
}

android {
    namespace = "com.kelyandev.fluxbiz"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kelyandev.fluxbiz"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.firebase.auth)
    implementation (libs.firebase.database)
    implementation (libs.firebase.firestore)
    implementation (libs.firebase.messaging)
    implementation (libs.firebase.crashlytics)
    implementation ("com.google.firebase:firebase-config")
    implementation ("com.google.firebase:firebase-perf")

    implementation (libs.preference)
    implementation(libs.swiperefreshlayout)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}