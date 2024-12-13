plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.heallinkapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.heallinkapp"
        minSdk = 30
        targetSdk = 34
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
        viewBinding = true
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.v190)
    implementation(libs.androidx.constraintlayout.v214)
    implementation (libs.google.material.v190)
    implementation (libs.androidx.viewpager2)



    // AndroidX Lifecycle
    implementation(libs.androidx.lifecycle.livedata.ktx.v262)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v262)


    // AndroidX Navigation
    implementation(libs.androidx.navigation.fragment.ktx.v275)
    implementation(libs.androidx.navigation.ui.ktx.v275)

    // AndroidX Legacy Support
    implementation(libs.androidx.legacy.support.v4)

    // AndroidX Fragment
    implementation(libs.androidx.fragment.ktx)

    // Retrofit & Interceptor
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // CardView
    implementation(libs.androidx.cardview)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.activity)

    ksp(libs.androidx.room.compiler)



    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    implementation(libs.google.maps.services)

    implementation(libs.glide)
    implementation (libs.androidx.media)

}
