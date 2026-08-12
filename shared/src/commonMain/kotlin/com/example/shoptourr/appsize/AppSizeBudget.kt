package com.example.shoptourr.appsize

/**
 * ch.10 install-size budget mirrored by `scripts/check-app-size.sh`.
 */
object AppSizeBudget {
    const val MAX_INSTALL_BYTES: Long = 40L * 1024L * 1024L

    fun isWithinBudget(sizeBytes: Long): Boolean = sizeBytes in 0..MAX_INSTALL_BYTES
}
