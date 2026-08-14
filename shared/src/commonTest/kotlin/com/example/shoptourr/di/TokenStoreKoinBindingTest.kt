package com.example.shoptourr.di

import com.example.shoptourr.data.settings.TokenStore
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.koin.core.context.stopKoin
import org.koin.core.error.InstanceCreationException

class TokenStoreKoinBindingTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TokenStore without extraModules fails fast`() {
        val app = initKoin()
        val error = assertFailsWith<InstanceCreationException> {
            app.koin.get<TokenStore>()
        }
        assertTrue(error.cause?.message.orEmpty().contains("platform extraModules"))
    }
}
