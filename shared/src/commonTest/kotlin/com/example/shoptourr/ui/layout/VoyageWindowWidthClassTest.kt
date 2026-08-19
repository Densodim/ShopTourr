package com.example.shoptourr.ui.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VoyageWindowWidthClassTest {

    @Test
    fun `material breakpoints split phone, fold, and tablet`() {
        assertEquals(VoyageWindowWidthClass.Compact, voyageWindowWidthClass(widthDp = 411f))
        assertEquals(VoyageWindowWidthClass.Compact, voyageWindowWidthClass(widthDp = 599f))
        assertEquals(VoyageWindowWidthClass.Medium, voyageWindowWidthClass(widthDp = 600f))
        assertEquals(VoyageWindowWidthClass.Medium, voyageWindowWidthClass(widthDp = 839f))
        assertEquals(VoyageWindowWidthClass.Expanded, voyageWindowWidthClass(widthDp = 840f))
        assertEquals(VoyageWindowWidthClass.Expanded, voyageWindowWidthClass(widthDp = 1200f))
    }

    @Test
    fun `trip list and detail share the screen only on expanded width`() {
        assertFalse(voyageWindowWidthClass(411f).showsTripListDetailPane)
        assertFalse(voyageWindowWidthClass(600f).showsTripListDetailPane)
        assertTrue(voyageWindowWidthClass(840f).showsTripListDetailPane)
    }
}
