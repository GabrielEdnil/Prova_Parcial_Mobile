package com.example.pokedex.compose

import com.example.pokedex.compose.domain.ErrorKind
import com.example.pokedex.compose.domain.Outcome
import com.example.pokedex.compose.domain.PokemonSummary
import com.example.pokedex.compose.ui.list.ListContent
import com.example.pokedex.compose.ui.list.ListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bulbasaur = PokemonSummary(1, "bulbasaur", "url1")
    private val charmander = PokemonSummary(4, "charmander", "url4")

    @Test
    fun showsContentWhenCacheHasItems() = runTest {
        val viewModel = ListViewModel(FakePokemonRepository(listOf(bulbasaur, charmander)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val content = viewModel.uiState.value.content
        assertTrue(content is ListContent.Content)
        assertEquals(2, (content as ListContent.Content).items.size)
        assertEquals("Bulbasaur", content.items.first().name)
    }

    @Test
    fun showsLoadingWhileFirstRefreshIsPending() = runTest {
        val repository = FakePokemonRepository().apply { refreshDelayMs = 1_000 }
        val viewModel = ListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(ListContent.Loading, viewModel.uiState.value.content)

        advanceUntilIdle()
        assertEquals(ListContent.Empty, viewModel.uiState.value.content)
    }

    @Test
    fun showsErrorWhenCacheIsEmptyAndRefreshFails() = runTest {
        val repository = FakePokemonRepository().apply {
            refreshOutcome = Outcome.Failure(ErrorKind.Network)
        }
        val viewModel = ListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(ListContent.Error(ErrorKind.Network), viewModel.uiState.value.content)
    }

    @Test
    fun keepsShowingCachedItemsWhenRefreshFails() = runTest {
        val repository = FakePokemonRepository(listOf(bulbasaur)).apply {
            refreshOutcome = Outcome.Failure(ErrorKind.Network)
        }
        val viewModel = ListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val content = viewModel.uiState.value.content as ListContent.Content
        assertTrue(content.showOfflineNotice)
        assertEquals(1, content.items.size)
    }

    @Test
    fun filtersItemsByQuery() = runTest {
        val viewModel = ListViewModel(FakePokemonRepository(listOf(bulbasaur, charmander)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onQueryChange("char")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("char", state.query)
        val content = state.content as ListContent.Content
        assertEquals(listOf("Charmander"), content.items.map { it.name })
    }

    @Test
    fun showsEmptyWhenQueryHasNoMatch() = runTest {
        val viewModel = ListViewModel(FakePokemonRepository(listOf(bulbasaur)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onQueryChange("zzz")
        advanceUntilIdle()

        assertEquals(ListContent.Empty, viewModel.uiState.value.content)
    }

    @Test
    fun marksFavoritesAndTogglesThem() = runTest {
        val repository = FakePokemonRepository(listOf(bulbasaur))
        val viewModel = ListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val item = (viewModel.uiState.value.content as ListContent.Content).items.single()
        assertEquals(false, item.isFavorite)

        viewModel.onFavoriteClick(item)
        advanceUntilIdle()

        val updated = (viewModel.uiState.value.content as ListContent.Content).items.single()
        assertEquals(true, updated.isFavorite)
    }
}
