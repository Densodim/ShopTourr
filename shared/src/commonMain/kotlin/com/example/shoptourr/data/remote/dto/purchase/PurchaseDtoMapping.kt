package com.example.shoptourr.data.remote.dto.purchase

import com.example.shoptourr.domain.model.Money
import com.example.shoptourr.domain.model.Purchase
import com.example.shoptourr.domain.model.PurchaseCategory
import com.example.shoptourr.domain.model.VatCalculator

internal fun PurchaseDto.toDomainPurchase(pendingSync: Boolean = false): Purchase {
    val amountMoney = Money.parse(amount.amount, amount.currency)
    val vatBreakdown = VatCalculator.breakdown(
        amount = amountMoney,
        vatRatePercent = vat.vatRatePercent,
        vatIncluded = vat.vatIncluded,
    )
    return Purchase(
        id = id,
        tripId = tripId,
        name = name,
        category = PurchaseCategory.valueOf(category.name),
        amount = amountMoney,
        vat = vatBreakdown,
        taxRefundEligible = taxRefundEligible,
        place = place,
        purchaseDate = purchaseDate,
        purchaseTime = purchaseTime,
        pendingSync = pendingSync,
        updatedAt = updatedAt,
    )
}
