package com.example.cozypuzzle.domain.usecase.garden

import com.example.cozypuzzle.core.customview.GardenCanvasView
import com.example.cozypuzzle.domain.model.GardenItem
import com.example.cozypuzzle.domain.repository.GardenRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

/** Places a new decoration at a free spot near the lawn centre. */
class PlaceGardenItemUseCase @Inject constructor(
    private val gardenRepository: GardenRepository
) {

    suspend operator fun invoke(itemType: String, existingItems: List<GardenItem>) {
        val position = findFreeSpot(existingItems)
        gardenRepository.upsertItem(
            GardenItem(
                id = UUID.randomUUID().toString(),
                itemType = itemType,
                x = position.first,
                y = position.second,
                rotation = DEFAULT_ROTATION,
                scale = GardenCanvasView.DEFAULT_ITEM_SCALE
            )
        )
    }

    /** Spirals outward from the centre until an unoccupied cell is found. */
    private fun findFreeSpot(items: List<GardenItem>): Pair<Float, Float> {
        val occupied = items.map { it.x to it.y }
        val candidates = buildList {
            for (ring in 0..RING_COUNT) {
                for (row in -ring..ring) {
                    for (col in -ring..ring) {
                        if (maxOf(abs(row), abs(col)) != ring) continue
                        add(CENTER + row * SPACING to CENTER + col * SPACING)
                    }
                }
            }
        }
        return candidates.firstOrNull { spot ->
            occupied.none { (x, y) ->
                abs(x - spot.first) < TOLERANCE && abs(y - spot.second) < TOLERANCE
            }
        } ?: (CENTER to CENTER)
    }

    private companion object {
        const val CENTER = 0.5f
        const val SPACING = 0.12f
        const val RING_COUNT = 4
        const val TOLERANCE = 0.05f
        const val DEFAULT_ROTATION = 0f
    }
}
