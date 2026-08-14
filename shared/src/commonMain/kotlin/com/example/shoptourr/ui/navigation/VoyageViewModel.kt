package com.example.shoptourr.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.example.shoptourr.presentation.base.BaseViewModel
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

@Composable
inline fun <reified T : BaseViewModel<*, *>> rememberVoyageViewModel(
    vararg parameters: Any?,
): T {
    val scope = currentKoinScope()
    val keys = parameters.toList()
    val vm = remember(keys) {
        if (keys.isEmpty()) {
            scope.get<T>()
        } else {
            scope.get<T> { parametersOf(*keys.toTypedArray()) }
        }
    }
    DisposableEffect(vm) {
        onDispose { vm.onCleared() }
    }
    return vm
}
