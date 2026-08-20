package com.example.shoptourr.data.widget

import com.example.shoptourr.domain.widget.BudgetWidgetContract
import com.example.shoptourr.domain.widget.BudgetWidgetCopy
import com.example.shoptourr.domain.widget.BudgetWidgetSnapshot
import com.example.shoptourr.domain.widget.BudgetWidgetStore
import platform.Foundation.NSUserDefaults

class IosAppGroupBudgetWidgetStore : BudgetWidgetStore {
    private val defaults = NSUserDefaults(suiteName = BudgetWidgetContract.APP_GROUP)

    override fun write(snapshot: BudgetWidgetSnapshot) {
        defaults.setObject(
            BudgetWidgetCopy.encode(snapshot),
            forKey = BudgetWidgetContract.JSON_KEY,
        )
    }

    override fun read(): BudgetWidgetSnapshot? {
        val raw = defaults.stringForKey(BudgetWidgetContract.JSON_KEY)
        return BudgetWidgetCopy.decode(raw)
    }
}
