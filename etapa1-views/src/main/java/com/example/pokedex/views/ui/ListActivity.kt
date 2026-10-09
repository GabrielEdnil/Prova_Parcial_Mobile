package com.example.pokedex.views.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedex.views.R
import com.example.pokedex.views.data.MockPokemonData
import com.example.pokedex.views.databinding.ActivityListBinding
import com.example.pokedex.views.model.Pokemon
import com.example.pokedex.views.util.applySystemBarsPadding

/** Tela 1: lista de Pokémon (RecyclerView). Ao tocar em um item, abre a tela de detalhes. */
class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()

        val pokemons = MockPokemonData.all

        binding.textCounter.text = getString(R.string.list_counter, pokemons.size)
        binding.textEmpty.isVisible = pokemons.isEmpty()
        binding.recyclerPokemon.layoutManager = LinearLayoutManager(this)
        binding.recyclerPokemon.adapter = PokemonAdapter(pokemons, ::openDetail)
    }

    /** Navegação por Intent explícita, passando dados para a próxima tela. */
    private fun openDetail(pokemon: Pokemon) {
        startActivity(DetailActivity.newIntent(this, pokemon))
    }
}
