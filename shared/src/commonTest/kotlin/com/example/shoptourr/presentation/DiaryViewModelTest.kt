package com.example.shoptourr.presentation

import com.example.shoptourr.domain.usecase.CreateDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.DeleteDiaryEntryUseCase
import com.example.shoptourr.domain.usecase.ObserveDiaryUseCase
import com.example.shoptourr.domain.usecase.RefreshDiaryUseCase
import com.example.shoptourr.fake.FakeDiaryRepository
import com.example.shoptourr.presentation.diary.DiaryIntent
import com.example.shoptourr.presentation.diary.DiaryViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `add entry updates observed days`() = runTest {
        val repo = FakeDiaryRepository()
        val vm = DiaryViewModel(
            tripId = "lisbon",
            observeDiary = ObserveDiaryUseCase(repo),
            refreshDiary = RefreshDiaryUseCase(repo),
            createEntry = CreateDiaryEntryUseCase(repo),
            deleteEntry = DeleteDiaryEntryUseCase(repo),
        )
        vm.onIntent(DiaryIntent.MoodChanged("happy"))
        vm.onIntent(DiaryIntent.TextChanged("Pasteis day"))
        vm.onIntent(DiaryIntent.Add)
        assertTrue(vm.state.value.days.flatMap { it.entries }.any { it.text == "Pasteis day" })
        assertEquals("", vm.state.value.textDraft)
        vm.onCleared()
    }

    @Test
    fun `validation maps to UiError`() = runTest {
        val repo = FakeDiaryRepository()
        val vm = DiaryViewModel(
            tripId = "lisbon",
            observeDiary = ObserveDiaryUseCase(repo),
            refreshDiary = RefreshDiaryUseCase(repo),
            createEntry = CreateDiaryEntryUseCase(repo),
            deleteEntry = DeleteDiaryEntryUseCase(repo),
        )
        vm.onIntent(DiaryIntent.TextChanged(""))
        vm.onIntent(DiaryIntent.Add)
        assertEquals("Проверьте поля", vm.state.value.error?.title)
        assertEquals("text", vm.state.value.error?.message)
        vm.onCleared()
    }
}
