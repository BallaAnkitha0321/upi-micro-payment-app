plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.upimicro"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.upimicro"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material UI
    implementation("com.google.android.material:material:1.11.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Retrofit (API calls)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")

    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    // Navigation
    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // QR Code generation (ZXing)
    implementation("com.google.zxing:core:3.5.1")

    // CameraX (for scanning)
    implementation("androidx.camera:camera-core:1.3.3")
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

    // ML Kit Barcode Scanner
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Swipe Refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Country Code Picker
    implementation("com.hbb20:ccp:2.7.0")

    // Google libphonenumber
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.31")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.android.libraries.places:places:3.4.0")

    //analytics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}