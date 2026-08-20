package com.example.shoptourr.data.widget

import com.example.shoptourr.domain.widget.BudgetWidgetContract
import com.example.shoptourr.domain.widget.BudgetWidgetCopy
import com.example.shoptourr.domain.widget.BudgetWidgetSnapshot
import com.example.shoptourr.domain.widget.BudgetWidgetStore
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsBudgetWidgetStore(
    private val settings: Settings,
) : BudgetWidgetStore {
    override fun write(snapshot: BudgetWidgetSnapshot) {
        settings[BudgetWidgetContract.JSON_KEY] = BudgetWidgetCopy.encode(snapshot)
    }

    override fun read(): BudgetWidgetSnapshot? =
        BudgetWidgetCopy.decode(settings.getStringOrNull(BudgetWidgetContract.JSON_KEY))
}
