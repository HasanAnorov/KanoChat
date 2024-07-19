plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.devtoolsKsp)
    alias(libs.plugins.pluginSerialization)
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
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

    //datastore used for preferences
    implementation (libs.androidx.datastore.preferences)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    //kotlin json serializer
    implementation(libs.kotlinx.serialization.json)

    //recording view
    implementation(libs.audiorecorderview)

    //gson
    implementation (libs.converter.gson)

    //image loading - coil
    implementation(libs.coil.compose)

    // add the following lines
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

}