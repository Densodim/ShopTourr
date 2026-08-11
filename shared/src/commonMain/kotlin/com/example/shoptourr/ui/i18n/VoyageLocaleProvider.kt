package com.example.shoptourr.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.usecase.ObservePreferencesUseCase
import com.example.shoptourr.i18n.AppLocale
import com.example.shoptourr.i18n.VoyageI18n
import org.koin.compose.koinInject

class VoyageStrings(val locale: AppLocale) {
    operator fun invoke(key: String, vararg pairs: Pair<String, Any>): String =
        VoyageI18n.t(
            locale = locale,
            key = key,
            vars = pairs.associate { it.first to it.second.toString() },
        )
}

val LocalVoyageStrings = staticCompositionLocalOf { VoyageStrings(AppLocale.RU) }

@Composable
fun t(key: String, vararg pairs: Pair<String, Any>): String =
    LocalVoyageStrings.current(key, *pairs)

@Composable
fun VoyageLocaleProvider(
    content: @Composable () -> Unit,
) {
    val observePreferences = koinInject<ObservePreferencesUseCase>()
    val preferences = produceState<UserPreferences?>(initialValue = null, observePreferences) {
        observePreferences().collect { value = it }
    }.value
    val strings = VoyageStrings(AppLocale.fromTag(preferences?.locale))
    CompositionLocalProvider(LocalVoyageStrings provides strings, content = content)
}
