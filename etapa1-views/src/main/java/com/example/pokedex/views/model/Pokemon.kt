package com.example.pokedex.views.model

/**
 * Modelo imutável exibido nas telas.
 * [description] e [habitat] são opcionais (podem ser null) e tratados na UI.
 */
data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val heightMeters: Double,
    val weightKg: Double,
    val description: String? = null,
    val habitat: String? = null,
)
