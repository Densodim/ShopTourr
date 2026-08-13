package com.example.shoptourr.data.local

import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.model.FeatureFlags
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsClientRemoteConfigStore(
    private val settings: Settings,
) : ClientRemoteConfigStore {
    private val state = MutableStateFlow(read())

    override fun observe(): Flow<ClientRemoteConfig?> = state.asStateFlow()
    override fun current(): ClientRemoteConfig? = state.value

    override fun save(config: ClientRemoteConfig) {
        settings[KEY] = json.encodeToString(config.toRecord())
        state.value = config
    }

    override fun clear() {
        settings.remove(KEY)
        state.value = null
    }

    private fun read(): ClientRemoteConfig? {
        val raw = settings.getStringOrNull(KEY) ?: return null
        val record = runCatching { json.decodeFromString<ClientRemoteConfigRecord>(raw) }.getOrNull()
            ?: return null
        return record.toDomain()
    }

    private companion object {
        const val KEY = "client.remote_config.json"
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }
    }
}

@Serializable
private data class ClientRemoteConfigRecord(
    val minAndroidBuild: Int,
    val minIosBuild: Int,
    val softMinAndroidBuild: Int? = null,
    val softMinIosBuild: Int? = null,
    val exportPdf: Boolean = true,
    val ocrAssist: Boolean = true,
    val nativeMaps: Boolean = false,
    val storeUrlAndroid: String? = null,
    val storeUrlIos: String? = null,
)

private fun ClientRemoteConfig.toRecord() = ClientRemoteConfigRecord(
    minAndroidBuild = minAndroidBuild,
    minIosBuild = minIosBuild,
    softMinAndroidBuild = softMinAndroidBuild,
    softMinIosBuild = softMinIosBuild,
    exportPdf = flags.exportPdf,
    ocrAssist = flags.ocrAssist,
    nativeMaps = flags.nativeMaps,
    storeUrlAndroid = storeUrlAndroid,
    storeUrlIos = storeUrlIos,
)

private fun ClientRemoteConfigRecord.toDomain() = ClientRemoteConfig(
    minAndroidBuild = minAndroidBuild,
    minIosBuild = minIosBuild,
    softMinAndroidBuild = softMinAndroidBuild,
    softMinIosBuild = softMinIosBuild,
    flags = FeatureFlags(
        exportPdf = exportPdf,
        ocrAssist = ocrAssist,
        nativeMaps = nativeMaps,
    ),
    storeUrlAndroid = storeUrlAndroid,
    storeUrlIos = storeUrlIos,
)
