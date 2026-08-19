package com.example.shoptourr.domain.model

/**
 * Turns a typed search box into an FTS5 MATCH prefix query.
 * Returns null when nothing searchable remains (caller should skip MATCH).
 */
object FtsQuery {
    fun fromUserInput(raw: String): String? {
        val tokens = raw.trim()
            .split(Regex("\\s+"))
            .map { token -> token.filter { it.isLetterOrDigit() }.lowercase() }
            .filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return null
        return tokens.joinToString(" ") { "$it*" }
    }
}
