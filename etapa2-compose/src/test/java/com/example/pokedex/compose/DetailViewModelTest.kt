package com.example.pokedex.compose

import com.example.pokedex.compose.domain.ErrorKind
import com.example.pokedex.compose.domain.NicknameError
import com.example.pokedex.compose.domain.Outcome
import com.example.pokedex.compose.ui.detail.DetailLoad
import com.example.pokedex.compose.ui.detail.DetailMessage
import com.example.pokedex.compose.ui.detail.DetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadsDetailContent() = runTest {
        val viewModel = DetailViewModel(1, FakePokemonRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.load is DetailLoad.Content)
    }

    @Test
    fun showsErrorWhenLoadFails() = runTest {
        val repository = FakePokemonRepository().apply {
            detailOutcome = Outcome.Failure(ErrorKind.Server)
        }
        val viewModel = DetailViewModel(1, repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(DetailLoad.Error(ErrorKind.Server), viewModel.uiState.value.load)
    }

    @Test
    fun invalidNicknameShowsErrorAndDoesNotSave() = runTest {
        val repository = FakePokemonRepository()
        val viewModel = DetailViewModel(1, repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onToggleFavorite()
        advanceUntilIdle()
        viewModel.onNicknameChange("")
        viewModel.onSaveClick()
        advanceUntilIdle()

        val form = viewModel.uiState.value.form
        assertEquals(NicknameError.Empty, form.nicknameError)
        assertNull(repository.favorites.value.getValue(1).nickname)
    }

    @Test
    fun validFormSavesFavoriteAndEmitsMessage() = runTest {
        val repository = FakePokemonRepository()
        val viewModel = DetailViewModel(1, repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onToggleFavorite()
        advanceUntilIdle()
        viewModel.onNicknameChange("  Bulba  ")
        viewModel.onNoteChange("Meu inicial favorito")
        viewModel.onSaveClick()
        advanceUntilIdle()

        val saved = repository.favorites.value.getValue(1)
        assertEquals("Bulba", saved.nickname)
        assertEquals("Meu inicial favorito", saved.note)
        assertEquals(DetailMessage.Saved, viewModel.uiState.value.message)

        viewModel.onMessageShown()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.message)
    }
}
