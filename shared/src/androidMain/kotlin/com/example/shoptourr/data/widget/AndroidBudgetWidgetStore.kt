package com.example.shoptourr.data.widget

import android.content.Context
import com.example.shoptourr.domain.widget.BudgetWidgetContract
import com.example.shoptourr.domain.widget.BudgetWidgetCopy
import com.example.shoptourr.domain.widget.BudgetWidgetSnapshot
import com.example.shoptourr.domain.widget.BudgetWidgetStore

class AndroidBudgetWidgetStore(
    context: Context,
) : BudgetWidgetStore {
    private val prefs = context.applicationContext.getSharedPreferences(
        BudgetWidgetContract.PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    override fun write(snapshot: BudgetWidgetSnapshot) {
        prefs.edit()
            .putString(BudgetWidgetContract.JSON_KEY, BudgetWidgetCopy.encode(snapshot))
            .apply()
    }

    override fun read(): BudgetWidgetSnapshot? =
        BudgetWidgetCopy.decode(prefs.getString(BudgetWidgetContract.JSON_KEY, null))
}
