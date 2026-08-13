package com.example.shoptourr.fake

import com.example.shoptourr.domain.repository.LocalSessionStore

class FakeLocalSessionStore : LocalSessionStore {
    var clearCalls: Int = 0
        private set

    override suspend fun clearUserData() {
        clearCalls += 1
    }
}
