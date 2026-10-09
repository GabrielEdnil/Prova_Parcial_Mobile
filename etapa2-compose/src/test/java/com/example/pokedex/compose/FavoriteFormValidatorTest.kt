package com.example.pokedex.compose

import com.example.pokedex.compose.domain.FavoriteFormValidator
import com.example.pokedex.compose.domain.NicknameError
import com.example.pokedex.compose.domain.NoteError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FavoriteFormValidatorTest {

    @Test
    fun blankNicknameIsEmptyError() {
        assertEquals(NicknameError.Empty, FavoriteFormValidator.validateNickname("   "))
    }

    @Test
    fun shortNicknameIsTooShort() {
        assertEquals(NicknameError.TooShort, FavoriteFormValidator.validateNickname("a"))
    }

    @Test
    fun longNicknameIsTooLong() {
        val tooLong = "a".repeat(FavoriteFormValidator.NICKNAME_MAX + 1)
        assertEquals(NicknameError.TooLong, FavoriteFormValidator.validateNickname(tooLong))
    }

    @Test
    fun nicknameWithSymbolsIsInvalid() {
        assertEquals(NicknameError.InvalidCharacters, FavoriteFormValidator.validateNickname("Pika@!"))
    }

    @Test
    fun validNicknameHasNoError() {
        assertNull(FavoriteFormValidator.validateNickname("Pika-chu 2"))
    }

    @Test
    fun noteAboveLimitIsError() {
        val tooLong = "x".repeat(FavoriteFormValidator.NOTE_MAX + 1)
        assertEquals(NoteError.TooLong, FavoriteFormValidator.validateNote(tooLong))
    }

    @Test
    fun emptyNoteIsValid() {
        assertNull(FavoriteFormValidator.validateNote(""))
    }
}
