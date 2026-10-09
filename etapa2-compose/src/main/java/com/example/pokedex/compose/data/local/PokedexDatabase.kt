package com.example.pokedex.compose.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonSummaryEntity::class, PokemonDetailEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PokedexDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun favoriteDao(): FavoriteDao
}
