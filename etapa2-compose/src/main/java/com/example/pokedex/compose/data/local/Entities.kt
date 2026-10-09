package com.example.pokedex.compose.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_summary")
data class PokemonSummaryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val cachedAt: Long,
)

@Entity(tableName = "pokemon_detail")
data class PokemonDetailEntity(
    @PrimaryKey val id: Int,
    val name: String,
    /** Tipos separados por vírgula (ex.: "grass,poison"). */
    val types: String,
    val abilities: String,
    val heightDecimeters: Int,
    val weightHectograms: Int,
    val cachedAt: Long,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val pokemonId: Int,
    val nickname: String?,
    val note: String?,
    val createdAt: Long,
)

/** Resultado do JOIN entre favoritos e o cache da lista. */
data class FavoriteWithSummary(
    val pokemonId: Int,
    val nickname: String?,
    val note: String?,
    val name: String?,
    val imageUrl: String?,
)
