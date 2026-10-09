package com.example.pokedex.compose

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pokedex.compose.domain.ErrorKind
import com.example.pokedex.compose.ui.list.ListContent
import com.example.pokedex.compose.ui.list.ListUiState
import com.example.pokedex.compose.ui.list.PokemonItemUi
import com.example.pokedex.compose.ui.list.PokemonListScreen
import com.example.pokedex.compose.ui.theme.PokedexTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PokemonListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun render(
        state: ListUiState,
        onRetry: () -> Unit = {},
        onItemClick: (PokemonItemUi) -> Unit = {},
    ) {
        composeRule.setContent {
            PokedexTheme(dynamicColor = false) {
                PokemonListScreen(
                    state = state,
                    onQueryChange = {},
                    onRefresh = {},
                    onRetry = onRetry,
                    onItemClick = onItemClick,
                    onFavoriteClick = {},
                    onOpenFavorites = {},
                )
            }
        }
    }

    @Test
    fun showsPokemonNamesWhenContent() {
        val items = listOf(
            PokemonItemUi(1, "Bulbasaur", "", false),
            PokemonItemUi(4, "Charmander", "", true),
        )
        render(ListUiState(content = ListContent.Content(items, isRefreshing = false, showOfflineNotice = false)))

        composeRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeRule.onNodeWithText("Charmander").assertIsDisplayed()
    }

    @Test
    fun showsErrorMessageAndRetryButton() {
        var retried = false
        render(
            state = ListUiState(content = ListContent.Error(ErrorKind.Network)),
            onRetry = { retried = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.error_network)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.action_retry)).performClick()
        assertEquals(true, retried)
    }

    @Test
    fun showsEmptyMessage() {
        render(ListUiState(content = ListContent.Empty))
        composeRule.onNodeWithText(context.getString(R.string.empty_list)).assertIsDisplayed()
    }

    @Test
    fun clickingAnItemCallsCallback() {
        var clicked: PokemonItemUi? = null
        val item = PokemonItemUi(25, "Pikachu", "", false)
        render(
            state = ListUiState(content = ListContent.Content(listOf(item), false, false)),
            onItemClick = { clicked = it },
        )

        composeRule.onNodeWithText("Pikachu").performClick()
        assertEquals(item, clicked)
    }
}
