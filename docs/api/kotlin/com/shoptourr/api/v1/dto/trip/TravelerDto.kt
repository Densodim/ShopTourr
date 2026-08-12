package com.shoptourr.api.v1.dto.trip

import java.util.UUID

data class TravelerDto(
    val id: UUID,
    val name: String,
    /** Hex accent color e.g. #FFD84D */
    val colorHex: String,
    /** Single letter / initials for avatar glyph. */
    val avatarGlyph: String?,
    val isOwner: Boolean,
)
