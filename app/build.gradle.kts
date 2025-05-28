plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.um.linkcamp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.um.linkcamp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }

}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.1.0"))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.firestore)
    implementation(libs.swiperefreshlayout)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.android.gms:play-services-base:18.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.github.dhaval2404:imagepicker:2.1")
    implementation ("com.github.DImuthuUpe:AndroidPdfViewer:3.1.0-beta.1")
    implementation ("com.cloudinary:cloudinary-android:3.0.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("com.intuit.sdp:sdp-android:1.0.6")
    implementation ("com.intuit.ssp:ssp-android:1.0.6")
    implementation ("com.makeramen:roundedimageview:2.3.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.11.0")
}
apply(plugin = "com.google.gms.google-services")
