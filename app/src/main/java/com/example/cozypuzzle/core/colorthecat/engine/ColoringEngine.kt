package com.example.cozypuzzle.core.colorthecat.engine

import com.example.cozypuzzle.core.colorthecat.model.ColorCatLevel
import com.example.cozypuzzle.core.colorthecat.model.ColorCatState
import com.example.cozypuzzle.core.colorthecat.model.ColoringAction

class ColoringEngine {

    fun createInitialState(level: ColorCatLevel): ColorCatState {
        val initialColor = level.paletteColors.firstOrNull() ?: 0xFFFFB6D2.toInt()
        return ColorCatState(
            level = level,
            regionColors = emptyMap(),
            selectedColor = initialColor,
            undoStack = emptyList(),
            hintsRemaining = ColorCatState.DEFAULT_HINTS,
            hintsUsed = 0,
            highlightedRegionId = null,
            isCompleted = false,
            isPaused = false,
            elapsedTimeMs = 0L,
            starsEarned = 0
        )
    }

    fun selectColor(state: ColorCatState, color: Int): ColorCatState {
        if (state.isCompleted || state.isPaused) return state
        return state.copy(selectedColor = color)
    }

    fun colorRegion(state: ColorCatState, regionId: String): ColorCatState {
        if (state.isCompleted || state.isPaused) return state
        val targetRegion = state.level.regions.firstOrNull { it.id == regionId } ?: return state

        val previousColor = state.regionColors[regionId] ?: targetRegion.defaultColor
        if (previousColor == state.selectedColor && state.regionColors.containsKey(regionId)) {
            return state
        }

        val updatedColors = state.regionColors.toMutableMap()
        updatedColors[regionId] = state.selectedColor

        val action = ColoringAction(
            regionId = regionId,
            previousColor = previousColor,
            newColor = state.selectedColor
        )
        val updatedUndoStack = state.undoStack + action

        // Clear highlighted hint if this region was the one highlighted
        val nextHighlight = if (state.highlightedRegionId == regionId) null else state.highlightedRegionId

        val allRequiredColored = state.level.regions
            .filter { it.isRequired }
            .all { updatedColors.containsKey(it.id) }

        val stars = if (allRequiredColored) {
            calculateStars(hintsUsed = state.hintsUsed, elapsedTimeMs = state.elapsedTimeMs)
        } else {
            0
        }

        return state.copy(
            regionColors = updatedColors,
            undoStack = updatedUndoStack,
            highlightedRegionId = nextHighlight,
            isCompleted = allRequiredColored,
            starsEarned = stars
        )
    }

    fun undo(state: ColorCatState): ColorCatState {
        if (state.undoStack.isEmpty() || state.isCompleted || state.isPaused) return state

        val lastAction = state.undoStack.last()
        val remainingStack = state.undoStack.dropLast(1)

        val updatedColors = state.regionColors.toMutableMap()
        val region = state.level.regions.firstOrNull { it.id == lastAction.regionId }
        val defaultColor = region?.defaultColor ?: 0xFFFFFFFF.toInt()

        if (lastAction.previousColor == defaultColor) {
            // It was previously uncolored, check if an earlier action on the same region exists
            val priorAction = remainingStack.lastOrNull { it.regionId == lastAction.regionId }
            if (priorAction != null) {
                updatedColors[lastAction.regionId] = priorAction.newColor
            } else {
                updatedColors.remove(lastAction.regionId)
            }
        } else {
            updatedColors[lastAction.regionId] = lastAction.previousColor
        }

        return state.copy(
            regionColors = updatedColors,
            undoStack = remainingStack
        )
    }

    fun reset(state: ColorCatState): ColorCatState {
        if (state.isCompleted) return state
        return state.copy(
            regionColors = emptyMap(),
            undoStack = emptyList(),
            highlightedRegionId = null,
            isCompleted = false,
            starsEarned = 0
        )
    }

    fun useHint(state: ColorCatState): Pair<ColorCatState, String?> {
        if (state.hintsRemaining <= 0 || state.isCompleted || state.isPaused) {
            return Pair(state, null)
        }

        val uncoloredRequired = state.level.regions.firstOrNull { region ->
            region.isRequired && !state.regionColors.containsKey(region.id)
        } ?: return Pair(state, null)

        val newState = state.copy(
            hintsRemaining = state.hintsRemaining - 1,
            hintsUsed = state.hintsUsed + 1,
            highlightedRegionId = uncoloredRequired.id
        )
        return Pair(newState, uncoloredRequired.id)
    }

    fun clearHint(state: ColorCatState): ColorCatState {
        if (state.highlightedRegionId == null) return state
        return state.copy(highlightedRegionId = null)
    }

    fun pause(state: ColorCatState): ColorCatState {
        if (state.isCompleted) return state
        return state.copy(isPaused = true)
    }

    fun resume(state: ColorCatState): ColorCatState {
        return state.copy(isPaused = false)
    }

    fun tickTime(state: ColorCatState, deltaMs: Long): ColorCatState {
        if (state.isPaused || state.isCompleted) return state
        return state.copy(elapsedTimeMs = state.elapsedTimeMs + deltaMs)
    }

    fun calculateStars(hintsUsed: Int, elapsedTimeMs: Long): Int {
        return when {
            hintsUsed <= 1 -> 3
            hintsUsed <= 2 -> 2
            else -> 1
        }
    }
}
