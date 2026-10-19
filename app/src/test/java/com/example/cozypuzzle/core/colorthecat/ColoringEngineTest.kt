package com.example.cozypuzzle.core.colorthecat

import com.example.cozypuzzle.core.colorthecat.engine.ColoringEngine
import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import com.example.cozypuzzle.core.colorthecat.model.ColorCatLevel
import com.example.cozypuzzle.core.colorthecat.model.ColorableRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ColoringEngineTest {

    private lateinit var engine: ColoringEngine
    private lateinit var sampleLevel: ColorCatLevel

    private val colorPink = 0xFFFFB6D2.toInt()
    private val colorBlue = 0xFF83D4F7.toInt()
    private val colorYellow = 0xFFFFE68C.toInt()

    @Before
    fun setUp() {
        engine = ColoringEngine()
        sampleLevel = ColorCatLevel(
            id = "test_cat_1",
            number = 1,
            title = "Test Cat",
            difficulty = ColorCatDifficulty.BASIC,
            viewBoxWidth = 300f,
            viewBoxHeight = 300f,
            regions = listOf(
                ColorableRegion(id = "ear_left", name = "Left Ear", pathData = "M 40 70 L 60 20 L 85 65 Z"),
                ColorableRegion(id = "ear_right", name = "Right Ear", pathData = "M 115 65 L 140 20 L 160 70 Z"),
                ColorableRegion(id = "face", name = "Face", pathData = "M 60 65 C 100 35 140 65 140 65 Z")
            ),
            paletteColors = listOf(colorPink, colorBlue, colorYellow)
        )
    }

    @Test
    fun `initial state has correct default values`() {
        val state = engine.createInitialState(sampleLevel)
        assertEquals(sampleLevel, state.level)
        assertEquals(colorPink, state.selectedColor)
        assertEquals(0, state.regionColors.size)
        assertEquals(3, state.hintsRemaining)
        assertEquals(0, state.hintsUsed)
        assertFalse(state.isCompleted)
        assertFalse(state.isPaused)
        assertEquals(3, state.totalRequiredRegions)
        assertEquals(0, state.coloredRequiredRegionsCount)
    }

    @Test
    fun `selecting color updates state selectedColor`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.selectColor(state, colorBlue)
        assertEquals(colorBlue, state.selectedColor)
    }

    @Test
    fun `coloring a region fills it and records undo action`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.selectColor(state, colorBlue)
        state = engine.colorRegion(state, "ear_left")

        assertEquals(colorBlue, state.regionColors["ear_left"])
        assertEquals(1, state.coloredRequiredRegionsCount)
        assertEquals(1, state.undoStack.size)
        assertEquals("ear_left", state.undoStack.first().regionId)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `recoloring a region changes its color without affecting other regions`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.selectColor(state, colorBlue)
        state = engine.colorRegion(state, "ear_left")

        state = engine.selectColor(state, colorYellow)
        state = engine.colorRegion(state, "ear_right")

        state = engine.selectColor(state, colorPink)
        state = engine.colorRegion(state, "ear_left") // Recoloring ear_left

        assertEquals(colorPink, state.regionColors["ear_left"])
        assertEquals(colorYellow, state.regionColors["ear_right"])
        assertEquals(2, state.coloredRequiredRegionsCount)
        assertEquals(3, state.undoStack.size)
    }

    @Test
    fun `undo restores previous state`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.selectColor(state, colorBlue)
        state = engine.colorRegion(state, "ear_left")
        assertEquals(1, state.regionColors.size)

        state = engine.undo(state)
        assertEquals(0, state.regionColors.size)
        assertEquals(0, state.undoStack.size)
    }

    @Test
    fun `undo restores prior colored state when recolored`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.selectColor(state, colorBlue)
        state = engine.colorRegion(state, "ear_left")

        state = engine.selectColor(state, colorYellow)
        state = engine.colorRegion(state, "ear_left")
        assertEquals(colorYellow, state.regionColors["ear_left"])

        state = engine.undo(state)
        assertEquals(colorBlue, state.regionColors["ear_left"])
    }

    @Test
    fun `reset clears all colored regions and undo stack`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.selectColor(state, colorBlue)
        state = engine.colorRegion(state, "ear_left")
        state = engine.colorRegion(state, "ear_right")

        state = engine.reset(state)
        assertTrue(state.regionColors.isEmpty())
        assertTrue(state.undoStack.isEmpty())
        assertFalse(state.isCompleted)
    }

    @Test
    fun `use hint highlights first uncolored required region`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.colorRegion(state, "ear_left")

        val (hintState, hintedRegionId) = engine.useHint(state)
        assertEquals("ear_right", hintedRegionId)
        assertEquals("ear_right", hintState.highlightedRegionId)
        assertEquals(2, hintState.hintsRemaining)
        assertEquals(1, hintState.hintsUsed)

        val clearedState = engine.clearHint(hintState)
        assertNull(clearedState.highlightedRegionId)
    }

    @Test
    fun `coloring all required regions completes level and awards 3 stars with low hint usage`() {
        var state = engine.createInitialState(sampleLevel)
        state = engine.selectColor(state, colorPink)
        state = engine.colorRegion(state, "ear_left")
        state = engine.colorRegion(state, "ear_right")
        assertFalse(state.isCompleted)

        state = engine.colorRegion(state, "face")
        assertTrue(state.isCompleted)
        assertEquals(3, state.starsEarned)
    }

    @Test
    fun `stars awarded decreases with more hint usage`() {
        var state = engine.createInitialState(sampleLevel)
        // Use 2 hints
        state = engine.useHint(state).first
        state = engine.useHint(state).first
        assertEquals(2, state.hintsUsed)

        state = engine.colorRegion(state, "ear_left")
        state = engine.colorRegion(state, "ear_right")
        state = engine.colorRegion(state, "face")

        assertTrue(state.isCompleted)
        assertEquals(2, state.starsEarned)
    }
}
