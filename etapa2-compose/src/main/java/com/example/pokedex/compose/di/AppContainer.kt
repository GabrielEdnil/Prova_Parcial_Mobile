package com.example.pokedex.compose.di

import android.content.Context
import androidx.room.Room
import com.example.pokedex.compose.data.DefaultPokemonRepository
import com.example.pokedex.compose.data.local.PokedexDatabase
import com.example.pokedex.compose.data.remote.PokeApiService
import com.example.pokedex.compose.domain.PokemonRepository
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Injeção manual de dependências: um contêiner simples criado no Application. */
interface AppContainer {
    val pokemonRepository: PokemonRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val api: PokeApiService = Retrofit.Builder()
        .baseUrl(PokeApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PokeApiService::class.java)

    private val database: PokedexDatabase by lazy {
        Room.databaseBuilder(appContext, PokedexDatabase::class.java, "pokedex.db").build()
    }

    override val pokemonRepository: PokemonRepository by lazy {
        DefaultPokemonRepository(
            api = api,
            pokemonDao = database.pokemonDao(),
            favoriteDao = database.favoriteDao(),
        )
    }
}
