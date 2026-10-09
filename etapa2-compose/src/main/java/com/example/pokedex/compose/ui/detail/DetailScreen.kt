package com.example.pokedex.compose.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pokedex.compose.R
import com.example.pokedex.compose.domain.FavoriteFormValidator
import com.example.pokedex.compose.domain.NicknameError
import com.example.pokedex.compose.domain.NoteError
import com.example.pokedex.compose.domain.PokemonDetail
import com.example.pokedex.compose.ui.common.ErrorMessage
import com.example.pokedex.compose.ui.common.LoadingIndicator
import com.example.pokedex.compose.ui.common.PokemonImage
import com.example.pokedex.compose.ui.common.capitalizeName

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    fallbackTitle: String,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonDetailScreen(
        state = state,
        fallbackTitle = fallbackTitle,
        onBack = onBack,
        onRetry = viewModel::load,
        onToggleFavorite = viewModel::onToggleFavorite,
        onNicknameChange = viewModel::onNicknameChange,
        onNoteChange = viewModel::onNoteChange,
        onSave = viewModel::onSaveClick,
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    state: DetailUiState,
    fallbackTitle: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = state.message?.let { stringResource(it.messageRes()) }

    // Evento de uma vez só: mostra o Snackbar e avisa o ViewModel que já foi exibido.
    LaunchedEffect(state.message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            onMessageShown()
        }
    }

    val title = (state.load as? DetailLoad.Content)?.pokemon?.name?.capitalizeName() ?: fallbackTitle

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {
            when (val load = state.load) {
                DetailLoad.Loading -> LoadingIndicator()
                is DetailLoad.Error -> ErrorMessage(kind = load.kind, onRetry = onRetry)
                is DetailLoad.Content -> DetailContent(
                    pokemon = load.pokemon,
                    state = state,
                    onToggleFavorite = onToggleFavorite,
                    onNicknameChange = onNicknameChange,
                    onNoteChange = onNoteChange,
                    onSave = onSave,
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    pokemon: PokemonDetail,
    state: DetailUiState,
    onToggleFavorite: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val name = pokemon.name.capitalizeName()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PokemonImage(
            imageUrl = pokemon.imageUrl,
            contentDescription = stringResource(R.string.pokemon_image, name),
            modifier = Modifier.size(200.dp),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.pokemon_number, pokemon.id),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.detail_types),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            pokemon.types.forEach { type -> TypeChip(type.capitalizeName()) }
        }

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.detail_height, pokemon.heightMeters), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.detail_weight, pokemon.weightKg), style = MaterialTheme.typography.bodyLarge)
        if (pokemon.abilities.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.detail_abilities,
                    pokemon.abilities.joinToString { it.capitalizeName() },
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(Modifier.height(24.dp))
        FavoriteButton(isFavorite = state.isFavorite, onClick = onToggleFavorite)

        AnimatedVisibility(visible = state.isFavorite) {
            FavoriteForm(
                form = state.form,
                onNicknameChange = onNicknameChange,
                onNoteChange = onNoteChange,
                onSave = onSave,
            )
        }
    }
}

@Composable
private fun TypeChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    val modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
    val icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
    val label = stringResource(if (isFavorite) R.string.favorite_button_remove else R.string.favorite_button_add)
    if (isFavorite) {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(label)
        }
    } else {
        Button(onClick = onClick, modifier = modifier) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(label)
        }
    }
}

@Composable
private fun FavoriteForm(
    form: FavoriteFormState,
    onNicknameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.form_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        OutlinedTextField(
            value = form.nickname,
            onValueChange = onNicknameChange,
            label = { Text(stringResource(R.string.form_nickname)) },
            singleLine = true,
            isError = form.nicknameError != null,
            supportingText = {
                val error = form.nicknameError
                if (error != null) {
                    Text(error.message())
                } else {
                    Text(
                        stringResource(R.string.form_counter, form.nickname.length, FavoriteFormValidator.NICKNAME_MAX)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.note,
            onValueChange = onNoteChange,
            label = { Text(stringResource(R.string.form_note)) },
            minLines = 2,
            isError = form.noteError != null,
            supportingText = {
                val error = form.noteError
                if (error != null) {
                    Text(error.message())
                } else {
                    Text(stringResource(R.string.form_counter, form.note.length, FavoriteFormValidator.NOTE_MAX))
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onSave,
            enabled = !form.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.form_save))
        }
    }
}

private fun DetailMessage.messageRes(): Int = when (this) {
    DetailMessage.FavoriteAdded -> R.string.msg_favorite_added
    DetailMessage.FavoriteRemoved -> R.string.msg_favorite_removed
    DetailMessage.Saved -> R.string.msg_saved
}

@Composable
private fun NicknameError.message(): String = when (this) {
    NicknameError.Empty -> stringResource(R.string.error_nickname_empty)
    NicknameError.TooShort -> stringResource(R.string.error_nickname_short, FavoriteFormValidator.NICKNAME_MIN)
    NicknameError.TooLong -> stringResource(R.string.error_nickname_long, FavoriteFormValidator.NICKNAME_MAX)
    NicknameError.InvalidCharacters -> stringResource(R.string.error_nickname_chars)
}

@Composable
private fun NoteError.message(): String = when (this) {
    NoteError.TooLong -> stringResource(R.string.error_note_long, FavoriteFormValidator.NOTE_MAX)
}
