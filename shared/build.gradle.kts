import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
        // Static Shared does not link sqlite; the iOS app links SQLCipher.
        // Gradle native tests still need a sqlite provider.
        iosTarget.binaries.configureEach {
            if (this !is Framework) {
                linkerOpts("-lsqlite3")
            }
        }
    }

    android {
        namespace = "com.example.shoptourr.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.json)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.filekit.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android)
            implementation(libs.sqlcipher.android)
            implementation(libs.androidx.sqlite)
            implementation(libs.koin.android)
            implementation(libs.androidx.security.crypto)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native)
            implementation(libs.sqliter.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.koin.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

sqldelight {
    linkSqlite.set(false)
    databases {
        create("VoyageDatabase") {
            packageName.set("com.example.shoptourr.db")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
