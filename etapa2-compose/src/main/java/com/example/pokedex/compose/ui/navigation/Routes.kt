package com.example.pokedex.compose.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Rotas do Navigation 3: cada rota é uma chave serializável (sobrevive à recriação do processo). */
@Serializable
data object ListRoute : NavKey

/** Argumentos passados na própria rota: id do Pokémon e nome (para o título enquanto carrega). */
@Serializable
data class DetailRoute(val pokemonId: Int, val pokemonName: String) : NavKey

@Serializable
data object FavoritesRoute : NavKey
