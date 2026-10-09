package com.example.pokedex.views.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.views.R
import com.example.pokedex.views.databinding.ItemPokemonBinding
import com.example.pokedex.views.model.Pokemon

class PokemonAdapter(
    private val items: List<Pokemon>,
    private val onItemClick: (Pokemon) -> Unit,
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(val binding: ItemPokemonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val binding = ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PokemonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = items[position]
        val context = holder.itemView.context
        with(holder.binding) {
            textName.text = pokemon.name
            textTypes.text = pokemon.types.joinToString(" • ")
            textNumber.text = context.getString(R.string.pokemon_number, pokemon.id)
            root.setOnClickListener { onItemClick(pokemon) }
        }
    }

    override fun getItemCount(): Int = items.size
}
