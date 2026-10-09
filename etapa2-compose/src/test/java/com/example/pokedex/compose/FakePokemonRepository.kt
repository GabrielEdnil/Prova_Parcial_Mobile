package com.example.pokedex.compose

import com.example.pokedex.compose.domain.Favorite
import com.example.pokedex.compose.domain.FavoriteItem
import com.example.pokedex.compose.domain.Outcome
import com.example.pokedex.compose.domain.PokemonDetail
import com.example.pokedex.compose.domain.PokemonRepository
import com.example.pokedex.compose.domain.PokemonSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Repository falso, em memória, para testar ViewModels sem rede nem banco. */
class FakePokemonRepository(
    initialSummaries: List<PokemonSummary> = emptyList(),
) : PokemonRepository {

    val summaries = MutableStateFlow(initialSummaries)
    val favorites = MutableStateFlow<Map<Int, Favorite>>(emptyMap())

    var refreshOutcome: Outcome<Unit> = Outcome.Success(Unit)
    var refreshDelayMs: Long = 0
    var detailOutcome: Outcome<PokemonDetail> = Outcome.Success(sampleDetail())

    override fun observePokemonList(): Flow<List<PokemonSummary>> = summaries

    override suspend fun refreshList(force: Boolean): Outcome<Unit> {
        if (refreshDelayMs > 0) delay(refreshDelayMs)
        return refreshOutcome
    }

    override suspend fun getPokemonDetail(id: Int): Outcome<PokemonDetail> = detailOutcome

    override fun observeFavoriteIds(): Flow<Set<Int>> = favorites.map { it.keys }

    override fun observeFavorite(id: Int): Flow<Favorite?> = favorites.map { it[id] }

    override fun observeFavoriteItems(): Flow<List<FavoriteItem>> =
        favorites.map { map ->
            map.values.map { FavoriteItem(it.pokemonId, it.nickname, it.note, "pokemon${it.pokemonId}", null) }
        }

    override suspend fun addFavorite(id: Int) {
        favorites.update { if (id in it) it else it + (id to Favorite(id, null, null)) }
    }

    override suspend fun saveFavoriteDetails(id: Int, nickname: String, note: String?) {
        favorites.update { it + (id to Favorite(id, nickname, note)) }
    }

    override suspend fun removeFavorite(id: Int) {
        favorites.update { it - id }
    }

    companion object {
        fun sampleDetail(id: Int = 1) = PokemonDetail(
            id = id,
            name = "bulbasaur",
            imageUrl = "https://example.com/$id.png",
            types = listOf("grass", "poison"),
            abilities = listOf("overgrow"),
            heightMeters = 0.7,
            weightKg = 6.9,
        )
    }
}
