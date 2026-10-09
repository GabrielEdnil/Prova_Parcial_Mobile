package com.example.pokedex.compose.domain

import kotlinx.coroutines.flow.Flow

/** Contrato da camada de dados. Interface para permitir fakes nos testes. */
interface PokemonRepository {

    /** Lista de Pokémon vinda do cache local (Room) – fonte única da verdade. */
    fun observePokemonList(): Flow<List<PokemonSummary>>

    /** Atualiza o cache a partir da API. Só vai à rede se o cache expirou ou [force] = true. */
    suspend fun refreshList(force: Boolean = false): Outcome<Unit>

    suspend fun getPokemonDetail(id: Int): Outcome<PokemonDetail>

    fun observeFavoriteIds(): Flow<Set<Int>>

    fun observeFavorite(id: Int): Flow<Favorite?>

    fun observeFavoriteItems(): Flow<List<FavoriteItem>>

    suspend fun addFavorite(id: Int)

    suspend fun saveFavoriteDetails(id: Int, nickname: String, note: String?)

    suspend fun removeFavorite(id: Int)
}
