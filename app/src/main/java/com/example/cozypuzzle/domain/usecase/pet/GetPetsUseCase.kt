package com.example.cozypuzzle.domain.usecase.pet

import com.example.cozypuzzle.domain.model.Pet
import com.example.cozypuzzle.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetsUseCase @Inject constructor(
    private val petRepository: PetRepository
) {

    operator fun invoke(): Flow<List<Pet>> = petRepository.observePets()
}
