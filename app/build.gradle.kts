plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.codigoaveriapsp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.codigoaveriapsp"
        minSdk = 25
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
}

dependencies {
    //BoM
    implementation(platform(libs.firebase.bom))
//FirebaseRecyclerAdapter
    implementation (libs.firebaseui.firebase.ui.database)
//RecyclerView
    implementation (libs.recyclerview)
//workmanager
    implementation ("androidx.work:work-runtime:2.7.1")
//Firebase
    implementation(libs.firebase.analytics)
    //user auth
    implementation("com.google.firebase:firebase-auth")
    //database
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}