import org.gradle.initialization.Environment.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.ishaanbhela.geeksformovies"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ishaanbhela.geeksformovies"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0001"

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

    packaging {
        exclude("META-INF/INDEX.LIST") // Exclude this specific file
        exclude("META-INF/io.netty.versions.properties")
        // You can add more excludes if needed
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
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //implementation(kotlin("script-runtime"))
    implementation(kotlin("script-runtime"))
}