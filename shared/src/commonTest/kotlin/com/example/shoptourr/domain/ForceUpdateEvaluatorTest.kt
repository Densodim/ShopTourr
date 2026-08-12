package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.ClientPlatform
import com.example.shoptourr.domain.model.ClientRemoteConfig
import com.example.shoptourr.domain.model.FeatureFlag
import com.example.shoptourr.domain.model.FeatureFlags
import com.example.shoptourr.domain.model.ForceUpdateAction
import com.example.shoptourr.domain.model.ForceUpdateEvaluator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ForceUpdateEvaluatorTest {

    private val config = ClientRemoteConfig(
        minAndroidBuild = 20,
        minIosBuild = 15,
        softMinAndroidBuild = 25,
        softMinIosBuild = 18,
    )

    @Test
    fun `hard blocks android below min build`() {
        val action = ForceUpdateEvaluator.evaluate(
            platform = ClientPlatform.ANDROID,
            currentBuild = 19,
            config = config,
        )
        assertEquals(ForceUpdateAction.HARD, action)
    }

    @Test
    fun `soft prompts android between min and soft min`() {
        val action = ForceUpdateEvaluator.evaluate(
            platform = ClientPlatform.ANDROID,
            currentBuild = 22,
            config = config,
        )
        assertEquals(ForceUpdateAction.SOFT, action)
    }

    @Test
    fun `none when android meets soft min`() {
        val action = ForceUpdateEvaluator.evaluate(
            platform = ClientPlatform.ANDROID,
            currentBuild = 25,
            config = config,
        )
        assertEquals(ForceUpdateAction.NONE, action)
    }

    @Test
    fun `uses ios thresholds for ios platform`() {
        assertEquals(
            ForceUpdateAction.HARD,
            ForceUpdateEvaluator.evaluate(ClientPlatform.IOS, 14, config),
        )
        assertEquals(
            ForceUpdateAction.SOFT,
            ForceUpdateEvaluator.evaluate(ClientPlatform.IOS, 16, config),
        )
        assertEquals(
            ForceUpdateAction.NONE,
            ForceUpdateEvaluator.evaluate(ClientPlatform.IOS, 18, config),
        )
    }

    @Test
    fun `without soft min only hard or none`() {
        val hardOnly = config.copy(softMinAndroidBuild = null, softMinIosBuild = null)
        assertEquals(
            ForceUpdateAction.HARD,
            ForceUpdateEvaluator.evaluate(ClientPlatform.ANDROID, 10, hardOnly),
        )
        assertEquals(
            ForceUpdateAction.NONE,
            ForceUpdateEvaluator.evaluate(ClientPlatform.ANDROID, 20, hardOnly),
        )
    }
}

class FeatureFlagsTest {

    @Test
    fun `reads named flags`() {
        val flags = FeatureFlags(exportPdf = true, ocrAssist = false, nativeMaps = true)
        assertTrue(flags.isEnabled(FeatureFlag.EXPORT_PDF))
        assertFalse(flags.isEnabled(FeatureFlag.OCR_ASSIST))
        assertTrue(flags.isEnabled(FeatureFlag.NATIVE_MAPS))
    }
}
