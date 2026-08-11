package com.example.shoptourr.domain.model

/**
 * Even split of a purchase across selected travelers (mila AddScreen "your share").
 */
object PurchaseSplitCalculator {
    fun share(amount: Money, participantCount: Int): Money {
        if (participantCount <= 0) return amount
        return amount.splitEqually(participantCount).first()
    }
}
