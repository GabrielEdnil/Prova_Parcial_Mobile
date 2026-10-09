package com.example.pokedex.compose.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pokedex.compose.R
import com.example.pokedex.compose.domain.ErrorKind
import com.example.pokedex.compose.ui.common.EmptyMessage
import com.example.pokedex.compose.ui.common.ErrorMessage
import com.example.pokedex.compose.ui.common.LoadingIndicator
import com.example.pokedex.compose.ui.common.PokemonImage

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onOpenDetail: (id: Int, name: String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesContentScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::retry,
        onItemClick = { item ->
            onOpenDetail(item.pokemonId, item.pokemonName ?: "#${item.pokemonId}")
        },
        onRemove = { viewModel.remove(it.pokemonId) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContentScreen(
    state: FavoritesUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (FavoriteItemUi) -> Unit,
    onRemove: (FavoriteItemUi) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (state) {
                FavoritesUiState.Loading -> LoadingIndicator()
                FavoritesUiState.Empty -> EmptyMessage(stringResource(R.string.favorites_empty))
                FavoritesUiState.Error -> ErrorMessage(kind = ErrorKind.Unknown, onRetry = onRetry)
                is FavoritesUiState.Content -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = state.items, key = { it.pokemonId }) { item ->
                        FavoriteRow(
                            item = item,
                            onClick = { onItemClick(item) },
                            onRemove = { onRemove(item) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteRow(
    item: FavoriteItemUi,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fallbackName = stringResource(R.string.favorites_unknown_name, item.pokemonId)
    val officialName = item.pokemonName ?: fallbackName
    val title = item.nickname ?: officialName

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PokemonImage(
                imageUrl = item.imageUrl,
                contentDescription = stringResource(R.string.pokemon_image, officialName),
                modifier = Modifier.size(64.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (item.nickname != null) {
                    Text(
                        text = stringResource(R.string.favorites_original_name, officialName),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.favorites_remove_named, title),
                )
            }
        }
    }
}
