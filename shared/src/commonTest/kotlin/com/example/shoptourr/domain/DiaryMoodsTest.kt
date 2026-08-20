package com.example.shoptourr.domain

import com.example.shoptourr.domain.model.DiaryMoods
import com.example.shoptourr.domain.validation.FieldRules
import com.example.shoptourr.domain.validation.MOOD_MAX
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiaryMoodsTest {

    @Test
    fun `preset moods fit the API column and FieldRules`() {
        assertEquals(6, DiaryMoods.all.size)
        DiaryMoods.all.forEach { mood ->
            assertTrue(mood.id.isNotBlank())
            assertTrue(mood.glyph.length in 1..MOOD_MAX, mood.glyph)
            assertTrue(FieldRules.isMood(mood.glyph), mood.glyph)
        }
        assertEquals(DiaryMoods.all.size, DiaryMoods.all.map { it.id }.distinct().size)
        assertEquals("😊", DiaryMoods.defaultGlyph)
    }
}
