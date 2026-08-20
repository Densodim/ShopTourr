package com.example.shoptourr.domain.model

data class DiaryMood(
    val id: String,
    val glyph: String,
    val labelKey: String,
)

object DiaryMoods {
    val all: List<DiaryMood> = listOf(
        DiaryMood("good", "😊", "mood_good"),
        DiaryMood("love", "😍", "mood_love"),
        DiaryMood("ok", "😐", "mood_ok"),
        DiaryMood("sad", "😢", "mood_sad"),
        DiaryMood("mad", "😤", "mood_mad"),
        DiaryMood("tired", "😴", "mood_tired"),
    )

    val defaultGlyph: String = all.first().glyph
}
