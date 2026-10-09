package com.example.pokedex.compose.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM pokemon_summary ORDER BY id")
    fun observeSummaries(): Flow<List<PokemonSummaryEntity>>

    @Upsert
    suspend fun upsertSummaries(items: List<PokemonSummaryEntity>)

    @Query("SELECT MAX(cachedAt) FROM pokemon_summary")
    suspend fun lastSummaryCachedAt(): Long?

    @Query("SELECT * FROM pokemon_detail WHERE id = :id")
    suspend fun getDetail(id: Int): PokemonDetailEntity?

    @Upsert
    suspend fun upsertDetail(item: PokemonDetailEntity)
}

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites WHERE pokemonId = :id")
    fun observe(id: Int): Flow<FavoriteEntity?>

    @Query("SELECT * FROM favorites WHERE pokemonId = :id")
    suspend fun get(id: Int): FavoriteEntity?

    @Query("SELECT pokemonId FROM favorites")
    fun observeIds(): Flow<List<Int>>

    @Query(
        """
        SELECT f.pokemonId AS pokemonId, f.nickname AS nickname, f.note AS note,
               s.name AS name, s.imageUrl AS imageUrl
        FROM favorites f
        LEFT JOIN pokemon_summary s ON s.id = f.pokemonId
        ORDER BY f.createdAt DESC
        """
    )
    fun observeWithSummary(): Flow<List<FavoriteWithSummary>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(favorite: FavoriteEntity)

    @Upsert
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE pokemonId = :id")
    suspend fun delete(id: Int)
}
