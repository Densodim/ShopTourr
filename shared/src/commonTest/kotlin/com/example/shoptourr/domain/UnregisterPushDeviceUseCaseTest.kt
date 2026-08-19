package com.example.shoptourr.domain

import com.example.shoptourr.data.push.InMemoryRegisteredPushDeviceStore
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.usecase.UnregisterPushDeviceUseCase
import com.example.shoptourr.fake.FakePushRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UnregisterPushDeviceUseCaseTest {

    @Test
    fun `blank stored id is a success no-op`() = runTest {
        val repo = FakePushRepository()
        val result = UnregisterPushDeviceUseCase(repo, InMemoryRegisteredPushDeviceStore())()
        assertTrue(result.isSuccess)
        assertEquals(0, repo.unregisterCalls)
    }

    @Test
    fun `unregisters and clears the stored id`() = runTest {
        val repo = FakePushRepository()
        val store = InMemoryRegisteredPushDeviceStore().apply { save("dev-9") }
        UnregisterPushDeviceUseCase(repo, store)().getOrThrow()
        assertEquals("dev-9", repo.lastUnregisteredId)
        assertNull(store.deviceId())
    }

    @Test
    fun `keeps the stored id when the server rejects`() = runTest {
        val repo = FakePushRepository().apply { unregisterError = AppError.Network }
        val store = InMemoryRegisteredPushDeviceStore().apply { save("dev-9") }
        val result = UnregisterPushDeviceUseCase(repo, store)()
        assertTrue(result.isFailure)
        assertEquals("dev-9", store.deviceId())
    }
}
