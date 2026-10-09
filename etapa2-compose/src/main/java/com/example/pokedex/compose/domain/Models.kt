package com.example.pokedex.compose.domain

/** Modelos de domínio: independentes de API (DTO) e de banco (Entity). Todos imutáveis. */
data class PokemonSummary(
    val id: Int,
    val name: String,
    val imageUrl: String,
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val abilities: List<String>,
    val heightMeters: Double,
    val weightKg: Double,
)

data class Favorite(
    val pokemonId: Int,
    val nickname: String?,
    val note: String?,
)

/** Favorito combinado com os dados do Pokémon em cache (que podem não existir ainda). */
data class FavoriteItem(
    val pokemonId: Int,
    val nickname: String?,
    val note: String?,
    val pokemonName: String?,
    val imageUrl: String?,
)

enum class ErrorKind { Network, Server, Unknown }

/** Resultado de uma operação que pode falhar, sem usar exceções no fluxo normal. */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val kind: ErrorKind) : Outcome<Nothing>
}
