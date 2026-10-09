package com.example.pokedex.compose.data

import com.example.pokedex.compose.data.local.FavoriteEntity
import com.example.pokedex.compose.data.local.FavoriteWithSummary
import com.example.pokedex.compose.data.local.PokemonDetailEntity
import com.example.pokedex.compose.data.local.PokemonSummaryEntity
import com.example.pokedex.compose.data.remote.PokemonDetailDto
import com.example.pokedex.compose.data.remote.PokemonListItemDto
import com.example.pokedex.compose.domain.Favorite
import com.example.pokedex.compose.domain.FavoriteItem
import com.example.pokedex.compose.domain.PokemonDetail
import com.example.pokedex.compose.domain.PokemonSummary

private const val ARTWORK_BASE_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"

private const val LIST_SEPARATOR = ","

fun artworkUrl(id: Int): String = "$ARTWORK_BASE_URL$id.png"

// DTO -> Entity
fun PokemonListItemDto.toSummaryEntity(cachedAt: Long): PokemonSummaryEntity? {
    // A URL termina com o id: https://pokeapi.co/api/v2/pokemon/25/
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
    return PokemonSummaryEntity(id = id, name = name, imageUrl = artworkUrl(id), cachedAt = cachedAt)
}

fun PokemonDetailDto.toDetailEntity(cachedAt: Long): PokemonDetailEntity =
    PokemonDetailEntity(
        id = id,
        name = name,
        types = types.sortedBy { it.slot }.joinToString(LIST_SEPARATOR) { it.type.name },
        abilities = abilities.joinToString(LIST_SEPARATOR) { it.ability.name },
        heightDecimeters = height,
        weightHectograms = weight,
        cachedAt = cachedAt,
    )

// Entity -> Domain
fun PokemonSummaryEntity.toDomain() = PokemonSummary(id = id, name = name, imageUrl = imageUrl)

fun PokemonDetailEntity.toDomain() = PokemonDetail(
    id = id,
    name = name,
    imageUrl = artworkUrl(id),
    types = types.splitToList(),
    abilities = abilities.splitToList(),
    heightMeters = heightDecimeters / 10.0,
    weightKg = weightHectograms / 10.0,
)

fun FavoriteEntity.toDomain() = Favorite(pokemonId = pokemonId, nickname = nickname, note = note)

fun FavoriteWithSummary.toDomain() = FavoriteItem(
    pokemonId = pokemonId,
    nickname = nickname,
    note = note,
    pokemonName = name,
    imageUrl = imageUrl,
)

private fun String.splitToList(): List<String> =
    split(LIST_SEPARATOR).filter { it.isNotBlank() }
