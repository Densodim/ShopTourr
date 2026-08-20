package com.example.shoptourr.ui.testing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VoyageTestTagsTest {

    @Test
    fun `maestro critical tags are stable snake case ids`() {
        val critical = listOf(
            VoyageTestTags.WELCOME_SIGN_IN,
            VoyageTestTags.LOGIN_EMAIL,
            VoyageTestTags.LOGIN_PASSWORD,
            VoyageTestTags.LOGIN_SUBMIT,
            VoyageTestTags.HOME_ROOT,
            VoyageTestTags.HOME_NEW_TRIP,
            VoyageTestTags.HOME_ADD_PURCHASE,
            VoyageTestTags.HOME_CURRENT_TRIP,
            VoyageTestTags.NEW_TRIP_SUBMIT,
            VoyageTestTags.ADD_PURCHASE_SUBMIT,
            VoyageTestTags.WELCOME_SIGN_UP,
            VoyageTestTags.ROUTE_MAP,
        )
        critical.forEach { tag ->
            assertTrue(tag.isNotBlank())
            assertTrue(tag.all { it.isLowerCase() || it == '_' || it.isDigit() })
            assertEquals(tag, tag.trim())
        }
    }
}
