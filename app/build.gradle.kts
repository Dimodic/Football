import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun readSecret(name: String): String {
    val props = Properties()
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use { props.load(it) }
        val v = props.getProperty(name)
        if (!v.isNullOrBlank()) return v.trim()
    }
    val env = System.getenv(name)
    return env?.trim().orEmpty()
}

android {
    namespace = "com.example.footballstats"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.footballstats"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val apiKey = readSecret("SSTATS_API_KEY")
        buildConfigField("String", "SSTATS_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "SSTATS_BASE_URL", "\"https://api.sstats.net/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            packaging {
                jniLibs.keepDebugSymbols.add("**/libandroidx.graphics.path.so")
                jniLibs.keepDebugSymbols.add("**/libdatastore_shared_counter.so")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.google.material)

    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.moshi)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.squareup.moshi)
    implementation(libs.squareup.moshi.kotlin)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)

    debugImplementation(libs.androidx.compose.ui.tooling)
}