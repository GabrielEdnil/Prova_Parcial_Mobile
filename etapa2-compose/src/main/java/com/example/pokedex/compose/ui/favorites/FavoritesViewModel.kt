package com.example.pokedex.compose.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pokedex.compose.domain.FavoriteItem
import com.example.pokedex.compose.domain.PokemonRepository
import com.example.pokedex.compose.ui.common.capitalizeName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoriteItemUi(
    val pokemonId: Int,
    /** Nome oficial já formatado; null se o Pokémon ainda não está no cache local. */
    val pokemonName: String?,
    val nickname: String?,
    val note: String?,
    val imageUrl: String?,
)

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Empty : FavoritesUiState
    data object Error : FavoritesUiState
    data class Content(val items: List<FavoriteItemUi>) : FavoritesUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<FavoritesUiState> = retryTrigger
        .flatMapLatest {
            repository.observeFavoriteItems()
                .map<List<FavoriteItem>, FavoritesUiState> { items ->
                    if (items.isEmpty()) FavoritesUiState.Empty
                    else FavoritesUiState.Content(items.map { it.toUi() })
                }
                .catch { emit(FavoritesUiState.Error) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading,
        )

    fun retry() {
        retryTrigger.value += 1
    }

    fun remove(pokemonId: Int) {
        viewModelScope.launch { repository.removeFavorite(pokemonId) }
    }

    private fun FavoriteItem.toUi() = FavoriteItemUi(
        pokemonId = pokemonId,
        pokemonName = pokemonName?.capitalizeName(),
        nickname = nickname,
        note = note,
        imageUrl = imageUrl,
    )

    companion object {
        fun factory(repository: PokemonRepository) = viewModelFactory {
            initializer { FavoritesViewModel(repository) }
        }
    }
}
