package com.example.shoptourr.di

import com.example.shoptourr.domain.model.ClientPlatform
import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigTest {

    @Test
    fun `release builds use production api`() {
        val android = AppConfig.forClient(isReleaseBuild = true, platform = ClientPlatform.ANDROID)
        val ios = AppConfig.forClient(isReleaseBuild = true, platform = ClientPlatform.IOS)
        assertEquals(AppConfig.PRODUCTION_API_BASE_URL, android.apiBaseUrl)
        assertEquals(AppConfig.PRODUCTION_API_BASE_URL, ios.apiBaseUrl)
    }

    @Test
    fun `debug android uses emulator loopback`() {
        val config = AppConfig.forClient(isReleaseBuild = false, platform = ClientPlatform.ANDROID)
        assertEquals(AppConfig.ANDROID_EMULATOR_LOCAL_API, config.apiBaseUrl)
    }

    @Test
    fun `debug ios uses simulator loopback`() {
        val config = AppConfig.forClient(isReleaseBuild = false, platform = ClientPlatform.IOS)
        assertEquals(AppConfig.IOS_SIMULATOR_LOCAL_API, config.apiBaseUrl)
    }

    @Test
    fun `jvm host tests talk to local boot on loopback`() {
        assertEquals("http://127.0.0.1:8080/api", AppConfig.JVM_LOCAL_API)
    }
}
