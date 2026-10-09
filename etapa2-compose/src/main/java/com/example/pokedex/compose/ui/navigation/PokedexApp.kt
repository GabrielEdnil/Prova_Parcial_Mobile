package com.example.pokedex.compose.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.pokedex.compose.domain.PokemonRepository
import com.example.pokedex.compose.ui.detail.DetailScreen
import com.example.pokedex.compose.ui.detail.DetailViewModel
import com.example.pokedex.compose.ui.favorites.FavoritesScreen
import com.example.pokedex.compose.ui.favorites.FavoritesViewModel
import com.example.pokedex.compose.ui.list.ListScreen
import com.example.pokedex.compose.ui.list.ListViewModel

@Composable
fun PokedexApp(repository: PokemonRepository) {
    // O back stack é uma lista que NÓS controlamos: navegar = add, voltar = remover o último.
    val backStack = rememberNavBackStack(ListRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Cada entry do back stack ganha o seu próprio ViewModelStore:
            // o ViewModel é descartado quando a tela sai do back stack.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<ListRoute> {
                val viewModel: ListViewModel = viewModel(factory = ListViewModel.factory(repository))
                ListScreen(
                    viewModel = viewModel,
                    onOpenDetail = { id, name -> backStack.add(DetailRoute(id, name)) },
                    onOpenFavorites = { backStack.add(FavoritesRoute) },
                )
            }
            entry<DetailRoute> { route ->
                val viewModel: DetailViewModel = viewModel(
                    factory = DetailViewModel.factory(route.pokemonId, repository)
                )
                DetailScreen(
                    viewModel = viewModel,
                    fallbackTitle = route.pokemonName,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
            entry<FavoritesRoute> {
                val viewModel: FavoritesViewModel = viewModel(
                    factory = FavoritesViewModel.factory(repository)
                )
                FavoritesScreen(
                    viewModel = viewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onOpenDetail = { id, name -> backStack.add(DetailRoute(id, name)) },
                )
            }
        },
    )
}
