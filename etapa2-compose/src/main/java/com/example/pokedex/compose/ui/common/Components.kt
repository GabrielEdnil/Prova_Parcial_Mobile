package com.example.pokedex.compose.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.pokedex.compose.R
import com.example.pokedex.compose.domain.ErrorKind
import java.util.Locale

@StringRes
fun ErrorKind.messageRes(): Int = when (this) {
    ErrorKind.Network -> R.string.error_network
    ErrorKind.Server -> R.string.error_server
    ErrorKind.Unknown -> R.string.error_unknown
}

/** "bulbasaur" -> "Bulbasaur" */
fun String.capitalizeName(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.loading)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
    }
}

@Composable
fun ErrorMessage(kind: ErrorKind, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(kind.messageRes()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PokemonImage(imageUrl: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
