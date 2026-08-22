package com.example.shoptourr.presentation

import app.cash.turbine.test
import com.example.shoptourr.domain.error.AppError
import com.example.shoptourr.domain.model.AuthSession
import com.example.shoptourr.domain.model.ThemeMode
import com.example.shoptourr.domain.model.User
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import com.example.shoptourr.domain.model.UserStats
import com.example.shoptourr.domain.usecase.ActivatePremiumUseCase
import com.example.shoptourr.domain.usecase.LogoutUseCase
import com.example.shoptourr.domain.usecase.ObservePreferencesUseCase
import com.example.shoptourr.domain.usecase.ObserveProfileUseCase
import com.example.shoptourr.domain.usecase.RefreshPreferencesUseCase
import com.example.shoptourr.domain.usecase.RefreshProfileUseCase
import com.example.shoptourr.domain.usecase.UpdatePreferencesUseCase
import com.example.shoptourr.domain.usecase.UpdateProfileUseCase
import com.example.shoptourr.fake.FakeAuthRepository
import com.example.shoptourr.fake.FakeUserRepository
import com.example.shoptourr.presentation.profile.ProfileIntent
import com.example.shoptourr.presentation.profile.ProfileUiEvent
import com.example.shoptourr.presentation.profile.ProfileViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val profile = UserProfile(
        id = "u1",
        displayName = "Mila",
        email = "mila@voyage.app",
        locale = "ru",
        preferredCurrency = "EUR",
        theme = ThemeMode.SYSTEM,
        pushNotificationsEnabled = true,
        memberSince = "2026-01-01",
        stats = UserStats(1, 1, 0),
    )
    private val prefs = UserPreferences("ru", "EUR", ThemeMode.SYSTEM, true, false)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createVm(
        userRepo: FakeUserRepository = FakeUserRepository(profile, prefs),
        authRepo: FakeAuthRepository = FakeAuthRepository(
            session = AuthSession("a", "r", 1, 1, User("u1", "Mila", "mila@voyage.app", "ru")),
        ),
    ) = ProfileViewModel(
        observeProfile = ObserveProfileUseCase(userRepo),
        observePreferences = ObservePreferencesUseCase(userRepo),
        refreshProfile = RefreshProfileUseCase(userRepo),
        refreshPreferences = RefreshPreferencesUseCase(userRepo),
        updateProfile = UpdateProfileUseCase(userRepo),
        updatePreferences = UpdatePreferencesUseCase(userRepo),
        activatePremium = ActivatePremiumUseCase(userRepo),
        logout = LogoutUseCase(authRepo),
    )

    @Test
    fun `loads profile and preferences on start`() = runTest {
        val vm = createVm()
        vm.state.test {
            var state = awaitItem()
            if (state.profile == null) state = awaitItem()
            assertEquals("Mila", state.profile?.displayName)
            assertEquals("EUR", state.preferences?.preferredCurrency)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
        vm.onCleared()
    }

    @Test
    fun `save profile updates display name`() = runTest {
        val repo = FakeUserRepository(profile, prefs)
        val vm = createVm(userRepo = repo)
        vm.onIntent(ProfileIntent.DisplayNameChanged("Nova"))
        vm.onIntent(ProfileIntent.SaveProfile)
        assertEquals("Nova", vm.state.value.profile?.displayName)
        assertEquals(1, repo.updateProfileCalls)
        vm.onCleared()
    }

    @Test
    fun `logout emits logged out event`() = runTest {
        val auth = FakeAuthRepository(
            session = AuthSession("a", "r", 1, 1, User("u1", "Mila", "mila@voyage.app", "ru")),
        )
        val vm = createVm(authRepo = auth)
        vm.events.test {
            vm.onIntent(ProfileIntent.Logout)
            assertIs<ProfileUiEvent.LoggedOut>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(auth.session)
        vm.onCleared()
    }

    @Test
    fun `validation error maps to the display name field`() = runTest {
        val vm = createVm()
        vm.onIntent(ProfileIntent.DisplayNameChanged(" "))
        vm.onIntent(ProfileIntent.SaveProfile)
        assertNull(vm.state.value.error)
        assertEquals("validation_name_required", vm.state.value.fieldErrors.displayName)
        vm.onCleared()
    }

    @Test
    fun `refresh failure surfaces UiError`() = runTest {
        val vm = createVm(
            userRepo = FakeUserRepository(
                profile = profile,
                preferences = prefs,
                refreshError = AppError.Unauthorized,
            )
        )
        vm.onIntent(ProfileIntent.Refresh)
        assertEquals("Сессия истекла", vm.state.value.error?.title)
        vm.onCleared()
    }
}
