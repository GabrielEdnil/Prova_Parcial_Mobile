package com.example.pokedex.compose.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTOs: espelham o JSON da PokéAPI (apenas os campos usados). */
@Serializable
data class PokemonListResponseDto(
    val results: List<PokemonListItemDto> = emptyList(),
)

@Serializable
data class PokemonListItemDto(
    val name: String,
    val url: String,
)

@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlotDto> = emptyList(),
    val abilities: List<AbilitySlotDto> = emptyList(),
)

@Serializable
data class TypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto,
)

@Serializable
data class AbilitySlotDto(
    @SerialName("is_hidden") val isHidden: Boolean = false,
    val ability: NamedResourceDto,
)

@Serializable
data class NamedResourceDto(
    val name: String,
    val url: String? = null,
)
