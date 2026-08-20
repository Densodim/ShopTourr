import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.multiplatform.settings)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.sentry.android)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.biometric)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.example.shoptourr"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.shoptourr"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        val sentryDsn = (System.getenv("SENTRY_DSN") ?: "")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
        fun envField(name: String): String =
            (System.getenv(name) ?: "").replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${envField("GOOGLE_WEB_CLIENT_ID")}\"")
        buildConfigField("String", "APPLE_SERVICES_ID", "\"${envField("APPLE_SERVICES_ID")}\"")
        manifestPlaceholders["MAPS_API_KEY"] = System.getenv("MAPS_API_KEY").orEmpty()
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    val uploadKeystore = System.getenv("ANDROID_KEYSTORE_FILE").orEmpty()
    if (uploadKeystore.isNotBlank()) {
        signingConfigs {
            create("release") {
                storeFile = file(uploadKeystore)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD").orEmpty()
                keyAlias = System.getenv("ANDROID_KEY_ALIAS").orEmpty()
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD").orEmpty()
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (uploadKeystore.isNotBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
