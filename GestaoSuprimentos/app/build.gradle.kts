plugins {
    id("com.android.application")
    // No AGP 9 o Kotlin já vem embutido: não precisa do plugin org.jetbrains.kotlin.android
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "br.com.gestaosuprimentos"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.gestaosuprimentos"
        minSdk = 26
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
