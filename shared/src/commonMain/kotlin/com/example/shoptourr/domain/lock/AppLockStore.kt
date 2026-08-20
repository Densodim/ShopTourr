package com.example.shoptourr.domain.lock

interface AppLockStore {
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
}
