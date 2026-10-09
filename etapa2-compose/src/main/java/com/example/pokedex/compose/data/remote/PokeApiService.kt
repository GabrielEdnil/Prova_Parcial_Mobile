package com.example.pokedex.compose.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** API pública, sem chave de acesso: https://pokeapi.co */
interface PokeApiService {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0,
    ): PokemonListResponseDto

    @GET("pokemon/{id}")
    suspend fun getPokemon(@Path("id") id: Int): PokemonDetailDto

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
