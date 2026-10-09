package com.example.pokedex.views.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.pokedex.views.R
import com.example.pokedex.views.data.MockPokemonData
import com.example.pokedex.views.databinding.ActivityDetailBinding
import com.example.pokedex.views.databinding.ViewTypeChipBinding
import com.example.pokedex.views.model.Pokemon
import com.example.pokedex.views.util.applySystemBarsPadding

/** Tela 2: detalhes do Pokémon recebido por Intent. O botão alterna o estado de favorito. */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var pokemon: Pokemon? = null
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarsPadding()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getIntExtra(EXTRA_POKEMON_ID, INVALID_ID)
        val nameFromIntent: String? = intent.getStringExtra(EXTRA_POKEMON_NAME)
        title = nameFromIntent ?: getString(R.string.detail_title_default)

        pokemon = MockPokemonData.findById(id)
        isFavorite = savedInstanceState?.getBoolean(STATE_FAVORITE) ?: false

        val current = pokemon
        if (current == null) {
            showNotFound()
            return
        }

        bindPokemon(current)
        renderFavorite(current)
        binding.buttonFavorite.setOnClickListener {
            isFavorite = !isFavorite
            renderFavorite(current)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_FAVORITE, isFavorite)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun bindPokemon(pokemon: Pokemon) {
        with(binding) {
            textName.text = pokemon.name
            textNumber.text = getString(R.string.pokemon_number, pokemon.id)
            textHeight.text = getString(R.string.detail_height, pokemon.heightMeters)
            textWeight.text = getString(R.string.detail_weight, pokemon.weightKg)

            // Valores opcionais: description e habitat podem ser null.
            textDescription.text = pokemon.description ?: getString(R.string.detail_no_description)
            textHabitat.isVisible = pokemon.habitat != null
            pokemon.habitat?.let { textHabitat.text = getString(R.string.detail_habitat, it) }

            // Componente XML reutilizável (view_type_chip) inflado dinamicamente.
            containerTypes.removeAllViews()
            pokemon.types.forEach { type ->
                val chip = ViewTypeChipBinding.inflate(layoutInflater, containerTypes, false)
                chip.textChip.text = type
                containerTypes.addView(chip.root)
            }
        }
    }

    /** Atualiza a interface de acordo com o estado de favorito. */
    private fun renderFavorite(pokemon: Pokemon) {
        binding.buttonFavorite.setText(if (isFavorite) R.string.favorite_remove else R.string.favorite_add)
        binding.textFavoriteStatus.text = getString(
            if (isFavorite) R.string.favorite_status_on else R.string.favorite_status_off,
            pokemon.name,
        )
    }

    private fun showNotFound() {
        binding.textName.setText(R.string.detail_not_found)
        binding.textNumber.isVisible = false
        binding.textHeight.isVisible = false
        binding.textWeight.isVisible = false
        binding.textDescription.isVisible = false
        binding.containerTypes.isVisible = false
        binding.buttonFavorite.isVisible = false
        binding.textFavoriteStatus.isVisible = false
    }

    companion object {
        private const val EXTRA_POKEMON_ID = "extra_pokemon_id"
        private const val EXTRA_POKEMON_NAME = "extra_pokemon_name"
        private const val STATE_FAVORITE = "state_favorite"
        private const val INVALID_ID = -1

        fun newIntent(context: Context, pokemon: Pokemon): Intent =
            Intent(context, DetailActivity::class.java)
                .putExtra(EXTRA_POKEMON_ID, pokemon.id)
                .putExtra(EXTRA_POKEMON_NAME, pokemon.name)
    }
}
