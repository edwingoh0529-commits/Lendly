plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.groupassignment2app"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.groupassignment2app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // ---- AndroidX + Material ----
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx)

    // Added: the app is built out of Fragments, and several screens use
    // RecyclerView, CardView and CoordinatorLayout.
    implementation("androidx.fragment:fragment:1.8.5")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // ---- Firebase ----
    // The BoM sets matching versions, so the lines under it have no version.
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")

    // ---- Images ----
    // Only used for photos stored as a web URL. Photos the user picks from
    // their phone are stored as Base64 text in Firestore instead, which keeps
    // the project on Firebase's free Spark plan (Cloud Storage needs Blaze).
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ---- Tests ----
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // ---- Sign in with Google ----
    // Credential Manager replaces the deprecated GoogleSignInClient. It shows
    // the system account sheet, so there is no password screen of our own.
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // Pull down to refresh, the standard Android gesture for "check again"
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
}
