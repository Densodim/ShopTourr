package com.example.shoptourr.domain

import com.example.shoptourr.domain.auth.GoogleSignInRetry
import com.example.shoptourr.domain.auth.GoogleSignInSequence
import com.example.shoptourr.domain.auth.GoogleSignInStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GoogleSignInSequenceTest {

    @Test
    fun `explicit button starts with returning-user one tap`() {
        assertEquals(GoogleSignInStep.OneTapAuthorized, GoogleSignInSequence.first)
    }

    @Test
    fun `no credential falls through one tap then picker then SiWG button`() {
        val afterOneTap = GoogleSignInSequence.next(
            GoogleSignInStep.OneTapAuthorized,
            GoogleSignInRetry.NoCredential,
        )
        assertEquals(GoogleSignInStep.AccountPicker, afterOneTap)
        val afterPicker = GoogleSignInSequence.next(
            GoogleSignInStep.AccountPicker,
            GoogleSignInRetry.NoCredential,
        )
        assertEquals(GoogleSignInStep.SignInButton, afterPicker)
        assertNull(
            GoogleSignInSequence.next(GoogleSignInStep.SignInButton, GoogleSignInRetry.NoCredential),
        )
    }

    @Test
    fun `cancel does not fall through to the next Google prompt`() {
        GoogleSignInStep.entries.forEach { step ->
            assertNull(GoogleSignInSequence.next(step, GoogleSignInRetry.Cancelled))
        }
    }
}
