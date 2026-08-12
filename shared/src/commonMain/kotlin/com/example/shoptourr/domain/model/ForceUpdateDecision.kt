package com.example.shoptourr.domain.model

data class ForceUpdateDecision(
    val action: ForceUpdateAction,
    val storeUrl: String?,
    val config: ClientRemoteConfig,
)
