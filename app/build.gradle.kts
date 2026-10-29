plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "app.spill"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.spill"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    // CI injects these from secrets; without them the release APK is signed with the debug key so it still installs.
    val keystore = System.getenv("SPILL_KEYSTORE")
    if (keystore != null) {
        signingConfigs.create("release") {
            storeFile = file(keystore)
            storePassword = System.getenv("SPILL_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("SPILL_KEY_ALIAS")
            keyPassword = System.getenv("SPILL_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            // ponytail: R8 off until the release build has been exercised on a device; turn on for a smaller APK.
            isMinifyEnabled = false
            signingConfig = signingConfigs[if (keystore != null) "release" else "debug"]
        }
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250517")
}
