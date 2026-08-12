package com.example.shoptourr.data

import com.example.shoptourr.data.local.InMemoryClientRemoteConfigStore
import com.example.shoptourr.data.remote.UserApi
import com.example.shoptourr.data.remote.createVoyageHttpClient
import com.example.shoptourr.data.remote.dto.user.ClientRemoteConfigDto
import com.example.shoptourr.data.remote.dto.user.FeatureFlagsDto
import com.example.shoptourr.data.remote.voyageJson
import com.example.shoptourr.data.repository.ClientRemoteConfigRepositoryImpl
import com.example.shoptourr.domain.model.FeatureFlag
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString

class ClientRemoteConfigRepositoryIntegrationTest {

    private val json = voyageJson()

    @Test
    fun `refresh app-config caches flags and thresholds`() = runTest {
        val dto = ClientRemoteConfigDto(
            minAndroidBuild = 20,
            minIosBuild = 15,
            softMinAndroidBuild = 25,
            softMinIosBuild = 18,
            flags = FeatureFlagsDto(exportPdf = true, ocrAssist = false, nativeMaps = true),
            storeUrlAndroid = "https://play.google.com/voyage",
            storeUrlIos = "https://apps.apple.com/voyage",
        )
        val engine = MockEngine { request ->
            require(request.url.encodedPath.endsWith("/me/app-config"))
            respond(
                content = ByteReadChannel(json.encodeToString(dto)),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val store = InMemoryClientRemoteConfigStore()
        val repo = ClientRemoteConfigRepositoryImpl(
            api = UserApi(createVoyageHttpClient("https://api.test", engine, { "t" }), "https://api.test"),
            localStore = store,
        )

        val config = repo.refresh().getOrThrow()
        assertEquals(20, config.minAndroidBuild)
        assertEquals(false, config.flags.isEnabled(FeatureFlag.OCR_ASSIST))
        assertTrue(config.flags.isEnabled(FeatureFlag.NATIVE_MAPS))
        assertEquals("https://play.google.com/voyage", config.storeUrlAndroid)
        assertEquals(20, store.observe().first()?.minAndroidBuild)
    }
}
