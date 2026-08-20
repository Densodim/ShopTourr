package com.example.shoptourr.data.widget

import com.example.shoptourr.domain.widget.BudgetWidgetRefresher

fun interface IosWidgetTimelineReloader {
    fun reload()
}

object IosWidgetBridge {
    var reloader: IosWidgetTimelineReloader? = null
}

class IosBudgetWidgetRefresher : BudgetWidgetRefresher {
    override fun reload() {
        IosWidgetBridge.reloader?.reload()
    }
}
