plugins {
    id("com.android.application")
}

android {
    namespace = "com.yourorg.pentest.monitor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourorg.pentest.monitor"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
}
