package com.example.shoptourr.presentation

import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.ExportFormat
import com.example.shoptourr.domain.model.ExportJob
import com.example.shoptourr.domain.model.ExportJobStatus
import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.model.UserStats
import com.example.shoptourr.domain.usecase.CreateExportUseCase
import com.example.shoptourr.domain.usecase.ObserveExportJobUseCase
import com.example.shoptourr.domain.usecase.ObservePremiumUseCase
import com.example.shoptourr.domain.usecase.RefreshExportJobUseCase
import com.example.shoptourr.fake.FakeExportRepository
import com.example.shoptourr.fake.FakeUserRepository
import com.example.shoptourr.presentation.export.ExportIntent
import com.example.shoptourr.presentation.export.ExportViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ExportViewModelTest {

    private val premiumProfile = UserProfile(
        id = "u1",
        displayName = "Mila",
        email = "mila@voyage.app",
        locale = "ru",
        preferredCurrency = "EUR",
        theme = ThemeMode.SYSTEM,
        pushNotificationsEnabled = true,
        memberSince = "2026-01-01",
        premiumPlan = PremiumPlan.PLUS,
        stats = UserStats(0, 0, 0),
    )

    @Test
    fun `create starts polling until ready`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val ready = ExportJob(
                id = "export-1",
                tripId = "lisbon",
                format = ExportFormat.PDF,
                status = ExportJobStatus.READY,
                downloadUrl = "https://cdn.example/file.pdf",
                createdAt = "2026-01-01T00:00:00Z",
                finishedAt = "2026-01-01T00:00:05Z",
            )
            val repo = FakeExportRepository(
                refreshSequence = listOf(
                    ready.copy(status = ExportJobStatus.RUNNING, downloadUrl = null, finishedAt = null),
                    ready,
                ),
            )
            val vm = ExportViewModel(
                tripId = "lisbon",
                observeExportJob = ObserveExportJobUseCase(repo),
                createExport = CreateExportUseCase(repo),
                refreshExportJob = RefreshExportJobUseCase(repo),
                observePremium = ObservePremiumUseCase(FakeUserRepository(profile = premiumProfile)),
                pollIntervalMs = 10L,
            )
            vm.onIntent(ExportIntent.Create)
            advanceTimeBy(50)
            assertEquals(ExportJobStatus.READY, vm.state.value.job?.status)
            assertEquals("https://cdn.example/file.pdf", vm.state.value.job?.downloadUrl)
            assertFalse(vm.state.value.isPolling)
            assertTrue(repo.refreshCalls >= 1)
            vm.onCleared()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `create failure maps UiError`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val vm = ExportViewModel(
                tripId = "lisbon",
                observeExportJob = ObserveExportJobUseCase(FakeExportRepository()),
                createExport = CreateExportUseCase(
                    FakeExportRepository(createError = AppError.Unauthorized),
                ),
                refreshExportJob = RefreshExportJobUseCase(FakeExportRepository()),
                observePremium = ObservePremiumUseCase(FakeUserRepository(profile = premiumProfile)),
            )
            vm.onIntent(ExportIntent.FormatChanged(ExportFormat.CSV))
            vm.onIntent(ExportIntent.Create)
            assertEquals("Session Expired", vm.state.value.error?.title)
            vm.onCleared()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `pdf without premium is blocked`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val free = premiumProfile.copy(premiumPlan = PremiumPlan.FREE)
            val vm = ExportViewModel(
                tripId = "lisbon",
                observeExportJob = ObserveExportJobUseCase(FakeExportRepository()),
                createExport = CreateExportUseCase(FakeExportRepository()),
                refreshExportJob = RefreshExportJobUseCase(FakeExportRepository()),
                observePremium = ObservePremiumUseCase(FakeUserRepository(profile = free)),
            )
            vm.onIntent(ExportIntent.Create)
            assertEquals("Premium Required", vm.state.value.error?.title)
            vm.onCleared()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
