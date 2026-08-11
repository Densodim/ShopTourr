package com.example.shoptourr.domain.repository

import com.example.shoptourr.domain.model.PremiumPlan
import com.example.shoptourr.domain.model.UpdatePreferencesDraft
import com.example.shoptourr.domain.model.UpdateProfileDraft
import com.example.shoptourr.domain.model.UserPreferences
import com.example.shoptourr.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(): Flow<UserProfile?>
    fun observePreferences(): Flow<UserPreferences?>
    suspend fun refreshProfile(): Result<UserProfile>
    suspend fun updateProfile(draft: UpdateProfileDraft): Result<UserProfile>
    suspend fun refreshPreferences(): Result<UserPreferences>
    suspend fun updatePreferences(draft: UpdatePreferencesDraft): Result<UserPreferences>
    suspend fun activatePremium(plan: PremiumPlan): Result<UserProfile>
}
