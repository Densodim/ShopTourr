package com.example.shoptourr.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
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

/** Counted noun in the current locale, e.g. "1 покупка" / "7 покупок". */
@Composable
fun tPlural(base: String, count: Int): String =
    VoyageI18n.plural(LocalVoyageStrings.current.locale, base, count)

@Composable
fun VoyageLocaleProvider(
    content: @Composable () -> Unit,
) {
    val observePreferences = koinInject<ObservePreferencesUseCase>()
    val preferences = produceState<UserPreferences?>(initialValue = null, observePreferences) {
        observePreferences().collect { value = it }
    }.value
    val locale = AppLocale.fromTag(preferences?.locale)
    SideEffect { VoyageI18n.currentLocale = locale }
    val strings = VoyageStrings(locale)
    CompositionLocalProvider(LocalVoyageStrings provides strings, content = content)
}
