package com.example.shoptourr.domain.usecase

import com.example.shoptourr.domain.repository.AuthRepository

class IsLoggedInUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Boolean = authRepository.isLoggedIn()
}
