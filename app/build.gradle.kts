plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    kotlin("kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.swings.vitamatrix"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.swings.vitamatrix"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    // وابستگی‌های پایه
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.viewpager2)

    // Retrofit و ابزارهای شبکه
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // مدیریت خطاها
    implementation(libs.customactivityoncrash)



    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    ksp("androidx.room:room-compiler:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.1.3")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.github.weidongjian:androidWheelView:1.0.0")
    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
    implementation("com.github.SimformSolutionsPvtLtd:SSImagePicker:2.3")

    // Wearable Data Layer
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation("androidx.wear:wear:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ابزارهای اضافی
    implementation(libs.toaster)


    // Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)

    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

//    // Feature module support for Fragments
//    implementation(libs.androidx.navigation.dynamic.features.fragment)
//
//    // Testing Navigation
//    androidTestImplementation(libs.androidx.navigation.testing)
//
//    // JSON serialization library, works with the Kotlin serialization plugin
//    implementation(libs.kotlinx.serialization.json)

    // وابستگی‌های تست
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
