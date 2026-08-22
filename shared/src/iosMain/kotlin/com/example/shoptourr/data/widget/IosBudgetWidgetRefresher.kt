package com.example.shoptourr.data.widget

import com.example.shoptourr.domain.widget.BudgetWidgetRefresher

fun interface IosWidgetTimelineReloader {
    fun reload()
}

object IosWidgetBridge {
    var reloader: IosWidgetTimelineReloader? = null
}

/** Swift cannot construct Kotlin fun interfaces; pass a closure instead. */
fun setIosWidgetReloader(reload: () -> Unit) {
    IosWidgetBridge.reloader = IosWidgetTimelineReloader { reload() }
}

class IosBudgetWidgetRefresher : BudgetWidgetRefresher {
    override fun reload() {
        IosWidgetBridge.reloader?.reload()
    }
}
