package com.example.cozypuzzle.domain.repository

import com.example.cozypuzzle.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {

    fun observePets(): Flow<List<Pet>>
}
