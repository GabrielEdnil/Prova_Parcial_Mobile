package com.example.pokedex.compose.data

import com.example.pokedex.compose.data.local.FavoriteDao
import com.example.pokedex.compose.data.local.FavoriteEntity
import com.example.pokedex.compose.data.local.PokemonDao
import com.example.pokedex.compose.data.remote.PokeApiService
import com.example.pokedex.compose.domain.ErrorKind
import com.example.pokedex.compose.domain.Favorite
import com.example.pokedex.compose.domain.FavoriteItem
import com.example.pokedex.compose.domain.Outcome
import com.example.pokedex.compose.domain.PokemonDetail
import com.example.pokedex.compose.domain.PokemonRepository
import com.example.pokedex.compose.domain.PokemonSummary
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Estratégia de cache: o Room é a fonte da verdade. A API só é chamada quando
 * o cache está vazio/expirado (TTL de 24h) ou quando o usuário força a atualização.
 * Se a rede falhar mas houver dados salvos, o app continua funcionando offline.
 */
class DefaultPokemonRepository(
    private val api: PokeApiService,
    private val pokemonDao: PokemonDao,
    private val favoriteDao: FavoriteDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val cacheTtlMillis: Long = TimeUnit.HOURS.toMillis(24),
) : PokemonRepository {

    override fun observePokemonList(): Flow<List<PokemonSummary>> =
        pokemonDao.observeSummaries().map { list -> list.map { it.toDomain() } }

    override suspend fun refreshList(force: Boolean): Outcome<Unit> {
        if (!force) {
            val lastCachedAt = pokemonDao.lastSummaryCachedAt()
            if (lastCachedAt != null && clock() - lastCachedAt < cacheTtlMillis) {
                return Outcome.Success(Unit)
            }
        }
        return when (val result = safeApiCall { api.getPokemonList(limit = LIST_LIMIT) }) {
            is Outcome.Success -> {
                val now = clock()
                pokemonDao.upsertSummaries(result.value.results.mapNotNull { it.toSummaryEntity(now) })
                Outcome.Success(Unit)
            }
            is Outcome.Failure -> result
        }
    }

    override suspend fun getPokemonDetail(id: Int): Outcome<PokemonDetail> {
        val cached = pokemonDao.getDetail(id)
        if (cached != null && clock() - cached.cachedAt < cacheTtlMillis) {
            return Outcome.Success(cached.toDomain())
        }
        return when (val result = safeApiCall { api.getPokemon(id) }) {
            is Outcome.Success -> {
                val entity = result.value.toDetailEntity(clock())
                pokemonDao.upsertDetail(entity)
                Outcome.Success(entity.toDomain())
            }
            // Sem rede, mas com dado antigo salvo: melhor mostrar o antigo do que um erro.
            is Outcome.Failure ->
                if (cached != null) Outcome.Success(cached.toDomain()) else result
        }
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> =
        favoriteDao.observeIds().map { it.toSet() }

    override fun observeFavorite(id: Int): Flow<Favorite?> =
        favoriteDao.observe(id).map { it?.toDomain() }

    override fun observeFavoriteItems(): Flow<List<FavoriteItem>> =
        favoriteDao.observeWithSummary().map { list -> list.map { it.toDomain() } }

    override suspend fun addFavorite(id: Int) {
        favoriteDao.insertIfAbsent(
            FavoriteEntity(pokemonId = id, nickname = null, note = null, createdAt = clock())
        )
    }

    override suspend fun saveFavoriteDetails(id: Int, nickname: String, note: String?) {
        val createdAt = favoriteDao.get(id)?.createdAt ?: clock()
        favoriteDao.upsert(
            FavoriteEntity(pokemonId = id, nickname = nickname, note = note, createdAt = createdAt)
        )
    }

    override suspend fun removeFavorite(id: Int) {
        favoriteDao.delete(id)
    }

    /** Converte exceções de rede em [Outcome.Failure], sem engolir o cancelamento da coroutine. */
    private suspend fun <T> safeApiCall(block: suspend () -> T): Outcome<T> =
        try {
            Outcome.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Outcome.Failure(ErrorKind.Network)
        } catch (e: HttpException) {
            Outcome.Failure(ErrorKind.Server)
        } catch (e: SerializationException) {
            Outcome.Failure(ErrorKind.Server)
        } catch (e: Exception) {
            Outcome.Failure(ErrorKind.Unknown)
        }

    private companion object {
        const val LIST_LIMIT = 151
    }
}
