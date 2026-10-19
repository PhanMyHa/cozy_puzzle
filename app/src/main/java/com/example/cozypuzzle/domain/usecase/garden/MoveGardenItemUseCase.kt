package com.example.cozypuzzle.domain.usecase.garden

import com.example.cozypuzzle.core.customview.GardenCanvasView
import com.example.cozypuzzle.domain.model.GardenItem
import com.example.cozypuzzle.domain.repository.GardenRepository
import javax.inject.Inject

/** Moves a decoration to a new normalized lawn position. */
class MoveGardenItemUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {

    suspend operator fun invoke(itemId: String, x: Float, y: Float, items: List<GardenItem>) {
        val item = items.firstOrNull { it.id == itemId } ?: return
        gardenRepository.upsertItem(
            item.copy(
                x = x.coerceIn(0f, 1f),
                y = y.coerceIn(0f, 1f),
                scale = item.scale.coerceAtLeast(GardenCanvasView.MIN_SCALE)
            )
        )
    }
}
