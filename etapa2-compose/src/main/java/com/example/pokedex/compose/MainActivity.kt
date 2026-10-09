package com.example.pokedex.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pokedex.compose.ui.navigation.PokedexApp
import com.example.pokedex.compose.ui.theme.PokedexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PokedexApplication).container
        setContent {
            PokedexTheme {
                PokedexApp(repository = container.pokemonRepository)
            }
        }
    }
}
