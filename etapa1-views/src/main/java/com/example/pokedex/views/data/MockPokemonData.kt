package com.example.pokedex.views.data

import com.example.pokedex.views.model.Pokemon

/** Dados simulados (mocks): nesta etapa não há API nem banco de dados. */
object MockPokemonData {

    val all: List<Pokemon> = listOf(
        Pokemon(
            id = 1, name = "Bulbasaur", types = listOf("Grama", "Veneno"),
            heightMeters = 0.7, weightKg = 6.9,
            description = "Uma semente estranha foi plantada em suas costas ao nascer.",
            habitat = "Campos",
        ),
        Pokemon(
            id = 4, name = "Charmander", types = listOf("Fogo"),
            heightMeters = 0.6, weightKg = 8.5,
            description = "A chama na ponta da cauda mostra o quanto ele está saudável.",
            habitat = "Montanhas",
        ),
        Pokemon(
            id = 7, name = "Squirtle", types = listOf("Água"),
            heightMeters = 0.5, weightKg = 9.0,
            description = null,
            habitat = "Rios",
        ),
        Pokemon(
            id = 25, name = "Pikachu", types = listOf("Elétrico"),
            heightMeters = 0.4, weightKg = 6.0,
            description = "Armazena eletricidade nas bochechas e a libera quando se sente ameaçado.",
            habitat = null,
        ),
        Pokemon(
            id = 39, name = "Jigglypuff", types = listOf("Normal", "Fada"),
            heightMeters = 0.5, weightKg = 5.5,
            description = "Sua canção faz qualquer um adormecer.",
            habitat = "Campos",
        ),
        Pokemon(
            id = 52, name = "Meowth", types = listOf("Normal"),
            heightMeters = 0.4, weightKg = 4.2,
            description = null,
            habitat = null,
        ),
        Pokemon(
            id = 94, name = "Gengar", types = listOf("Fantasma", "Veneno"),
            heightMeters = 1.5, weightKg = 40.5,
            description = "Dizem que se esconde nas sombras para roubar o calor de quem passa.",
            habitat = "Cavernas",
        ),
        Pokemon(
            id = 133, name = "Eevee", types = listOf("Normal"),
            heightMeters = 0.3, weightKg = 6.5,
            description = "Sua estrutura genética instável permite que evolua de várias formas.",
            habitat = "Cidades",
        ),
        Pokemon(
            id = 143, name = "Snorlax", types = listOf("Normal"),
            heightMeters = 2.1, weightKg = 460.0,
            description = "Come cerca de 400 kg de comida por dia e dorme logo em seguida.",
            habitat = "Montanhas",
        ),
        Pokemon(
            id = 150, name = "Mewtwo", types = listOf("Psíquico"),
            heightMeters = 2.0, weightKg = 122.0,
            description = null,
            habitat = "Laboratório",
        ),
    )

    fun findById(id: Int): Pokemon? = all.firstOrNull { it.id == id }
}
