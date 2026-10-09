package com.example.pokedex.compose.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pokedex.compose.domain.ErrorKind
import com.example.pokedex.compose.domain.Outcome
import com.example.pokedex.compose.domain.PokemonRepository
import com.example.pokedex.compose.domain.PokemonSummary
import com.example.pokedex.compose.ui.common.capitalizeName
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Modelo de apresentação: já formatado para a UI (diferente do modelo de domínio). */
data class PokemonItemUi(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val isFavorite: Boolean,
)

sealed interface ListContent {
    data object Loading : ListContent
    data class Error(val kind: ErrorKind) : ListContent
    data object Empty : ListContent
    data class Content(
        val items: List<PokemonItemUi>,
        val isRefreshing: Boolean,
        val showOfflineNotice: Boolean,
    ) : ListContent
}

data class ListUiState(
    val query: String = "",
    val content: ListContent = ListContent.Loading,
)

private sealed interface RefreshState {
    data object Idle : RefreshState
    data object Loading : RefreshState
    data class Failed(val kind: ErrorKind) : RefreshState
}

class ListViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Loading)
    private var refreshJob: Job? = null

    // O texto digitado aparece na hora; o filtro espera 250 ms sem digitação.
    @OptIn(FlowPreview::class)
    private val filterQuery = query.debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MS }

    val uiState: StateFlow<ListUiState> = combine(
        query,
        filterQuery,
        repository.observePokemonList(),
        repository.observeFavoriteIds(),
        refreshState,
    ) { rawQuery, filter, summaries, favoriteIds, refresh ->
        buildState(rawQuery, filter, summaries, favoriteIds, refresh)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ListUiState(),
    )

    init {
        refresh()
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /** Cancela uma atualização em andamento antes de iniciar outra. */
    fun refresh(force: Boolean = false) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshState.value = RefreshState.Loading
            refreshState.value = when (val result = repository.refreshList(force)) {
                is Outcome.Success -> RefreshState.Idle
                is Outcome.Failure -> RefreshState.Failed(result.kind)
            }
        }
    }

    fun onFavoriteClick(item: PokemonItemUi) {
        viewModelScope.launch {
            if (item.isFavorite) repository.removeFavorite(item.id) else repository.addFavorite(item.id)
        }
    }

    private fun buildState(
        rawQuery: String,
        filter: String,
        summaries: List<PokemonSummary>,
        favoriteIds: Set<Int>,
        refresh: RefreshState,
    ): ListUiState {
        val term = filter.trim()
        val filtered = summaries.filter { it.name.contains(term, ignoreCase = true) }
        val content = when {
            summaries.isEmpty() -> when (refresh) {
                RefreshState.Loading -> ListContent.Loading
                is RefreshState.Failed -> ListContent.Error(refresh.kind)
                RefreshState.Idle -> ListContent.Empty
            }
            filtered.isEmpty() -> ListContent.Empty
            else -> ListContent.Content(
                items = filtered.map { it.toUi(isFavorite = it.id in favoriteIds) },
                isRefreshing = refresh is RefreshState.Loading,
                showOfflineNotice = refresh is RefreshState.Failed,
            )
        }
        return ListUiState(query = rawQuery, content = content)
    }

    private fun PokemonSummary.toUi(isFavorite: Boolean) = PokemonItemUi(
        id = id,
        name = name.capitalizeName(),
        imageUrl = imageUrl,
        isFavorite = isFavorite,
    )

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 250L

        fun factory(repository: PokemonRepository) = viewModelFactory {
            initializer { ListViewModel(repository) }
        }
    }
}
