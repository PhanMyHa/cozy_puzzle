package com.example.cozypuzzle.domain.usecase.garden

import com.example.cozypuzzle.domain.model.GardenItem
import com.example.cozypuzzle.domain.repository.GardenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGardenUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {

    operator fun invoke(): Flow<List<GardenItem>> = gardenRepository.observeGardenItems()
}
