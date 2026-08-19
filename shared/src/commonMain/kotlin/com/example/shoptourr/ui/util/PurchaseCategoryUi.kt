package com.example.shoptourr.ui.util

import com.example.shoptourr.domain.model.PurchaseCategory

/** Category glyphs mirror CATEGORIES_META in the design source. */
fun PurchaseCategory.emoji(): String = when (this) {
    PurchaseCategory.FOOD -> "🍽"
    PurchaseCategory.TRANSPORT -> "🚆"
    PurchaseCategory.SOUVENIRS -> "🎁"
    PurchaseCategory.HOTEL -> "🛏"
    PurchaseCategory.CULTURE -> "🏛"
    PurchaseCategory.OTHER -> "✦"
}

/**
 * Catalog key for the category name. Screens used to print `name.lowercase()`,
 * which showed an English enum ("souvenirs") inside otherwise Russian copy.
 */
fun PurchaseCategory.labelKey(): String = when (this) {
    PurchaseCategory.FOOD -> "cat_food"
    PurchaseCategory.TRANSPORT -> "cat_transport"
    PurchaseCategory.SOUVENIRS -> "cat_souvenirs"
    PurchaseCategory.HOTEL -> "cat_hotel"
    PurchaseCategory.CULTURE -> "cat_culture"
    PurchaseCategory.OTHER -> "cat_other"
}

/** `"0.5150"` → `52` — the API sends the share as a fraction string. */
fun sharePercent(share: String): Int {
    val value = share.toDoubleOrNull() ?: return 0
    // Tolerate both conventions rather than rendering 5150% if it ever changes.
    val fraction = if (value > 1.0) value / 100.0 else value
    return (fraction * 100).toInt().coerceIn(0, 100)
}
