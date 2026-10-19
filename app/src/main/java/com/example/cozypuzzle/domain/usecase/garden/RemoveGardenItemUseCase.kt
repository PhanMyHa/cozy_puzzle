package com.example.cozypuzzle.domain.usecase.garden

import com.example.cozypuzzle.domain.repository.GardenRepository
import javax.inject.Inject

class RemoveGardenItemUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {

    suspend operator fun invoke(itemId: String) {
        gardenRepository.removeItem(itemId)
    }
}
