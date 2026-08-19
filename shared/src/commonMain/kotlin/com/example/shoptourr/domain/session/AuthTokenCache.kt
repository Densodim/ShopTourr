package com.example.shoptourr.domain.session

/**
 * In-process HTTP auth cache (Ktor Bearer). Distinct from [com.example.shoptourr.data.settings.TokenStore]:
 * logout must clear both or the next request still sends the old Authorization header.
 */
fun interface AuthTokenCache {
    fun clear()
}

object NoOpAuthTokenCache : AuthTokenCache {
    override fun clear() = Unit
}

class RecordingAuthTokenCache : AuthTokenCache {
    var clearCalls: Int = 0
        private set

    override fun clear() {
        clearCalls += 1
    }
}
