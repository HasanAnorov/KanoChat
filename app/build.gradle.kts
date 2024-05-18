plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.ierusalem.androchat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ierusalem.androchat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // Allow references to generated code
    kapt {
        correctErrorTypes = true
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

        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation (libs.landscapist.glide)
    implementation (libs.androidx.ui.viewbinding)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation (libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.google.android.material)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //datastore used for multi language
    implementation (libs.androidx.datastore.preferences)

    //hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")

    //kotlin json serializer
    implementation(libs.kotlinx.serialization.json)

    //image loading - coil
    implementation(libs.coil.compose)

    //landscapist
    implementation (libs.landscapist.glide)

    //Android Studio 4.0.0 Java 8 library desugaring in D8
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // PYDroid
    implementation ("com.github.pyamsoft.pydroid:arch:27.0.1")
    implementation ("com.github.pyamsoft.pydroid:ui:27.0.1")

    // Ktor
    implementation ("io.ktor:ktor-client-core:1.6.3")
    implementation ("io.ktor:ktor-client-cio:1.6.3")
    implementation ("io.ktor:ktor-client-serialization:1.6.3")
    implementation ("io.ktor:ktor-client-websockets:1.6.3")
    implementation ("io.ktor:ktor-client-logging:1.6.3")
    implementation ("ch.qos.logback:logback-classic:1.2.6")
}