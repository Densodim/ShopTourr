package com.example.shoptourr.analytics

interface AnalyticsConsentStore {
    fun isGranted(): Boolean
    fun setGranted(granted: Boolean)
}

class InMemoryAnalyticsConsentStore(
    private var granted: Boolean = false,
) : AnalyticsConsentStore {
    override fun isGranted(): Boolean = granted
    override fun setGranted(granted: Boolean) {
        this.granted = granted
    }
}
