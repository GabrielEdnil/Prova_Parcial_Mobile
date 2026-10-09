package com.example.pokedex.compose.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pokedex.compose.R
import com.example.pokedex.compose.ui.common.EmptyMessage
import com.example.pokedex.compose.ui.common.ErrorMessage
import com.example.pokedex.compose.ui.common.LoadingIndicator
import com.example.pokedex.compose.ui.common.PokemonImage

/** Versão "com estado": conecta o ViewModel à UI. */
@Composable
fun ListScreen(
    viewModel: ListViewModel,
    onOpenDetail: (id: Int, name: String) -> Unit,
    onOpenFavorites: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonListScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRefresh = { viewModel.refresh(force = true) },
        onRetry = { viewModel.refresh(force = true) },
        onItemClick = { onOpenDetail(it.id, it.name) },
        onFavoriteClick = viewModel::onFavoriteClick,
        onOpenFavorites = onOpenFavorites,
    )
}

/** Versão "sem estado": só recebe dados e eventos (fácil de testar e de pré-visualizar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    state: ListUiState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (PokemonItemUi) -> Unit,
    onFavoriteClick: (PokemonItemUi) -> Unit,
    onOpenFavorites: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_title)) },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                    IconButton(onClick = onOpenFavorites) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.action_open_favorites),
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
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.search_label)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Animação implícita entre estados distintos (carregando / erro / vazio / conteúdo).
            AnimatedContent(
                targetState = state.content,
                contentKey = { it::class },
                label = "listContent",
                modifier = Modifier.fillMaxSize(),
            ) { content ->
                when (content) {
                    ListContent.Loading -> LoadingIndicator()
                    is ListContent.Error -> ErrorMessage(kind = content.kind, onRetry = onRetry)
                    ListContent.Empty -> EmptyMessage(
                        text = stringResource(
                            if (state.query.isBlank()) R.string.empty_list else R.string.empty_search
                        )
                    )
                    is ListContent.Content -> PokemonList(
                        content = content,
                        onItemClick = onItemClick,
                        onFavoriteClick = onFavoriteClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun PokemonList(
    content: ListContent.Content,
    onItemClick: (PokemonItemUi) -> Unit,
    onFavoriteClick: (PokemonItemUi) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = content.isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (content.showOfflineNotice) {
            Text(
                text = stringResource(R.string.offline_notice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = content.items, key = { it.id }) { item ->
                PokemonRow(
                    item = item,
                    onClick = { onItemClick(item) },
                    onFavoriteClick = { onFavoriteClick(item) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonRow(
    item: PokemonItemUi,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
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
                contentDescription = stringResource(R.string.pokemon_image, item.name),
                modifier = Modifier.size(64.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.pokemon_number, item.id),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(
                        if (item.isFavorite) R.string.remove_favorite_named else R.string.add_favorite_named,
                        item.name,
                    ),
                    tint = if (item.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
