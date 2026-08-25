package com.example.cozypuzzle.domain.model

enum class PetSpecies {
    CAT,
    BUNNY,
    BEAR,
    FROG,
    CHICK
}

data class Pet(
    val id: String,
    val name: String,
    val level: Int,
    val experience: Int,
    val species: PetSpecies
)
