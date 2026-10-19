package com.example.cozypuzzle.domain.usecase.garden

import com.example.cozypuzzle.domain.model.GardenItem
import com.example.cozypuzzle.domain.repository.GardenRepository
import javax.inject.Inject

/** Rotates a decoration by absolute degrees. */
class RotateGardenItemUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {

    suspend operator fun invoke(itemId: String, rotation: Float, items: List<GardenItem>) {
        val item = items.firstOrNull { it.id == itemId } ?: return
        gardenRepository.upsertItem(item.copy(rotation = normalize(rotation)))
    }

    private fun normalize(rotation: Float): Float {
        val normalized = rotation % FULL_ROTATION
        return if (normalized < 0f) normalized + FULL_ROTATION else normalized
    }

    private companion object {
        const val FULL_ROTATION = 360f
    }
}
