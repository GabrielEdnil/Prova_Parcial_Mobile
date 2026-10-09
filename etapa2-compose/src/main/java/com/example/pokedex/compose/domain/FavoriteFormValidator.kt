package com.example.pokedex.compose.domain

enum class NicknameError { Empty, TooShort, TooLong, InvalidCharacters }

enum class NoteError { TooLong }

/** Regras de validação do formulário de favorito (puras, fáceis de testar). */
object FavoriteFormValidator {
    const val NICKNAME_MIN = 2
    const val NICKNAME_MAX = 20
    const val NOTE_MAX = 120

    fun validateNickname(input: String): NicknameError? {
        val text = input.trim()
        return when {
            text.isEmpty() -> NicknameError.Empty
            text.length < NICKNAME_MIN -> NicknameError.TooShort
            text.length > NICKNAME_MAX -> NicknameError.TooLong
            !text.all { it.isLetterOrDigit() || it == ' ' || it == '-' } -> NicknameError.InvalidCharacters
            else -> null
        }
    }

    fun validateNote(input: String): NoteError? =
        if (input.trim().length > NOTE_MAX) NoteError.TooLong else null
}
