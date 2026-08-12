package com.shoptourr.api.v1.dto.common

data class PageRequestDto(
    page: Int = 0,
    size: Int = 20,
    sort: String? = null,
) {
    val page: Int = if (page < 0) 0 else page
    val size: Int = when {
        size < 1 -> 20
        size > 100 -> 100
        else -> size
    }
    val sort: String = if (sort.isNullOrBlank()) "createdAt,desc" else sort
}
