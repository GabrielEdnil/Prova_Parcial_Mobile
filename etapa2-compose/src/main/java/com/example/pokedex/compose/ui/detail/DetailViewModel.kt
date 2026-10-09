package com.example.pokedex.compose.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pokedex.compose.domain.ErrorKind
import com.example.pokedex.compose.domain.FavoriteFormValidator
import com.example.pokedex.compose.domain.NicknameError
import com.example.pokedex.compose.domain.NoteError
import com.example.pokedex.compose.domain.Outcome
import com.example.pokedex.compose.domain.PokemonDetail
import com.example.pokedex.compose.domain.PokemonRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DetailLoad {
    data object Loading : DetailLoad
    data class Content(val pokemon: PokemonDetail) : DetailLoad
    data class Error(val kind: ErrorKind) : DetailLoad
}

enum class DetailMessage { FavoriteAdded, FavoriteRemoved, Saved }

data class FavoriteFormState(
    val nickname: String = "",
    val note: String = "",
    val nicknameError: NicknameError? = null,
    val noteError: NoteError? = null,
    val submitAttempted: Boolean = false,
    val isSaving: Boolean = false,
)

data class DetailUiState(
    val load: DetailLoad = DetailLoad.Loading,
    val isFavorite: Boolean = false,
    val form: FavoriteFormState = FavoriteFormState(),
    val message: DetailMessage? = null,
)

class DetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonRepository,
) : ViewModel() {

    private val load = MutableStateFlow<DetailLoad>(DetailLoad.Loading)
    private val form = MutableStateFlow(FavoriteFormState())
    private val message = MutableStateFlow<DetailMessage?>(null)
    private var loadJob: Job? = null

    val uiState: StateFlow<DetailUiState> = combine(
        load,
        repository.observeFavorite(pokemonId),
        form,
        message,
    ) { loadState, favorite, formState, msg ->
        DetailUiState(
            load = loadState,
            isFavorite = favorite != null,
            form = formState,
            message = msg,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailUiState(),
    )

    init {
        load()
        prefillForm()
    }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            load.value = DetailLoad.Loading
            load.value = when (val result = repository.getPokemonDetail(pokemonId)) {
                is Outcome.Success -> DetailLoad.Content(result.value)
                is Outcome.Failure -> DetailLoad.Error(result.kind)
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            val isFavorite = repository.observeFavorite(pokemonId).first() != null
            if (isFavorite) {
                repository.removeFavorite(pokemonId)
                form.value = FavoriteFormState()
                message.value = DetailMessage.FavoriteRemoved
            } else {
                repository.addFavorite(pokemonId)
                message.value = DetailMessage.FavoriteAdded
            }
        }
    }

    fun onNicknameChange(value: String) {
        form.update { current ->
            current.copy(
                nickname = value,
                nicknameError = if (current.submitAttempted) FavoriteFormValidator.validateNickname(value) else null,
            )
        }
    }

    fun onNoteChange(value: String) {
        form.update { current ->
            current.copy(
                note = value,
                noteError = if (current.submitAttempted) FavoriteFormValidator.validateNote(value) else null,
            )
        }
    }

    fun onSaveClick() {
        val current = form.value
        val nicknameError = FavoriteFormValidator.validateNickname(current.nickname)
        val noteError = FavoriteFormValidator.validateNote(current.note)
        if (nicknameError != null || noteError != null) {
            form.update {
                it.copy(submitAttempted = true, nicknameError = nicknameError, noteError = noteError)
            }
            return
        }
        viewModelScope.launch {
            form.update { it.copy(submitAttempted = true, isSaving = true) }
            repository.saveFavoriteDetails(
                id = pokemonId,
                nickname = current.nickname.trim(),
                note = current.note.trim().ifBlank { null },
            )
            form.update { it.copy(isSaving = false) }
            message.value = DetailMessage.Saved
        }
    }

    fun onMessageShown() {
        message.value = null
    }

    /** Se já era favorito antes (ex.: app reaberto), preenche o formulário com o que foi salvo. */
    private fun prefillForm() {
        viewModelScope.launch {
            repository.observeFavorite(pokemonId).first()?.let { favorite ->
                form.update {
                    it.copy(nickname = favorite.nickname.orEmpty(), note = favorite.note.orEmpty())
                }
            }
        }
    }

    companion object {
        fun factory(pokemonId: Int, repository: PokemonRepository) = viewModelFactory {
            initializer { DetailViewModel(pokemonId, repository) }
        }
    }
}
