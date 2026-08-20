package com.example.shoptourr.ui.navigation

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.example.shoptourr.navigation.VoyageNavigationTarget

fun VoyageNavigationTarget.toScreen(): Screen = when (this) {
    VoyageNavigationTarget.Home -> MainShellVoyageScreen
    is VoyageNavigationTarget.TripDetail -> TripDetailVoyageScreen(tripId)
    is VoyageNavigationTarget.TripAlerts -> AlertsVoyageScreen(tripId)
    is VoyageNavigationTarget.TripTaxFree -> TaxFreeVoyageScreen(tripId)
    is VoyageNavigationTarget.TripRoute -> RouteVoyageScreen(tripId)
    is VoyageNavigationTarget.AddPurchase -> {
        val id = tripId?.trim().orEmpty()
        if (id.isEmpty()) MainShellVoyageScreen else AddPurchaseVoyageScreen(id)
    }
}

fun Navigator.applyDeepLink(target: VoyageNavigationTarget) {
    when (target) {
        VoyageNavigationTarget.Home -> replaceAll(MainShellVoyageScreen)
        is VoyageNavigationTarget.AddPurchase -> {
            val id = target.tripId?.trim().orEmpty()
            if (id.isEmpty()) {
                replaceAll(MainShellVoyageScreen)
            } else {
                replaceAll(MainShellVoyageScreen)
                push(AddPurchaseVoyageScreen(id))
            }
        }
        else -> {
            replaceAll(MainShellVoyageScreen)
            push(target.toScreen())
        }
    }
}
