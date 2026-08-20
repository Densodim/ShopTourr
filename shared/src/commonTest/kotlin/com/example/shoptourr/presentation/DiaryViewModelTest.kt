package com.example.shoptourr.presentation

import com.example.shoptourr.domain.model.DiaryMoods
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
        val vm = createVm(repo)
        vm.onIntent(DiaryIntent.MoodChanged("😍"))
        vm.onIntent(DiaryIntent.TextChanged("Pasteis day"))
        vm.onIntent(DiaryIntent.Add)
        assertTrue(vm.state.value.days.flatMap { it.entries }.any { it.text == "Pasteis day" })
        assertEquals("", vm.state.value.textDraft)
        vm.onCleared()
    }

    @Test
    fun `add uses the smiling mood until a chip is picked`() = runTest {
        val repo = FakeDiaryRepository()
        val vm = createVm(repo)
        assertEquals(DiaryMoods.defaultGlyph, vm.state.value.moodDraft)
        vm.onIntent(DiaryIntent.TextChanged("Pasteis day"))
        vm.onIntent(DiaryIntent.Add)
        val entry = vm.state.value.days.flatMap { it.entries }.single()
        assertEquals("😊", entry.mood)
        assertEquals("Pasteis day", entry.text)
        vm.onCleared()
    }

    @Test
    fun `mood chip updates the draft`() = runTest {
        val vm = createVm()
        vm.onIntent(DiaryIntent.MoodChanged("😢"))
        assertEquals("😢", vm.state.value.moodDraft)
        vm.onCleared()
    }

    @Test
    fun `validation maps to UiError`() = runTest {
        val vm = createVm()
        vm.onIntent(DiaryIntent.TextChanged(""))
        vm.onIntent(DiaryIntent.Add)
        assertEquals("Проверьте поля", vm.state.value.error?.title)
        assertEquals("text", vm.state.value.error?.message)
        vm.onCleared()
    }

    private fun createVm(repo: FakeDiaryRepository = FakeDiaryRepository()) = DiaryViewModel(
        tripId = "lisbon",
        observeDiary = ObserveDiaryUseCase(repo),
        refreshDiary = RefreshDiaryUseCase(repo),
        createEntry = CreateDiaryEntryUseCase(repo),
        deleteEntry = DeleteDiaryEntryUseCase(repo),
    )
}
