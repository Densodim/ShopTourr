package com.example.shoptourr.ui.layout

/**
 * Material 3 window-width breakpoints. Compact is a phone; Medium is a fold inner
 * display; Expanded is a tablet where the trip list and detail can share a row.
 */
enum class VoyageWindowWidthClass {
    Compact,
    Medium,
    Expanded,
    ;

    val showsTripListDetailPane: Boolean get() = this == Expanded
}

fun voyageWindowWidthClass(widthDp: Float): VoyageWindowWidthClass = when {
    widthDp >= 840f -> VoyageWindowWidthClass.Expanded
    widthDp >= 600f -> VoyageWindowWidthClass.Medium
    else -> VoyageWindowWidthClass.Compact
}
