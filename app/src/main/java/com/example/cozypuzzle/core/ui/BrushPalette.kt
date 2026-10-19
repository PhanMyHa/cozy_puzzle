package com.example.cozypuzzle.core.ui

/**
 * A single selectable watercolor brush color.
 */
data class BrushColor(
    val id: String,
    val name: String,
    /** Base ARGB color of the stroke. */
    val color: Int,
    /** Slightly darker companion used for accents such as flower centers. */
    val accent: Int
)

/**
 * The cute default palette. Renderers must look this list up instead of
 * hard-coding colors; new entries can be appended freely.
 */
object BrushPalette {

    const val DEFAULT_BRUSH_ID = "pink"

    val brushes: List<BrushColor> = listOf(
        BrushColor("pink", "Pink", 0xFFF48FB1.toInt(), 0xFFC2185B.toInt()),
        BrushColor("blue", "Blue", 0xFF81D4FA.toInt(), 0xFF0277BD.toInt()),
        BrushColor("purple", "Purple", 0xFFCE93D8.toInt(), 0xFF7B1FA2.toInt()),
        BrushColor("green", "Green", 0xFFA5D6A7.toInt(), 0xFF2E7D32.toInt()),
        BrushColor("yellow", "Yellow", 0xFFFFF176.toInt(), 0xFFF9A825.toInt()),
        BrushColor("orange", "Orange", 0xFFFFB74D.toInt(), 0xFFE65100.toInt())
    )

    private val byId: Map<String, BrushColor> = brushes.associateBy { it.id }

    fun byIdOrNull(id: String?): BrushColor? = byId[id]

    fun byId(id: String): BrushColor = byId[id] ?: brushes.first()

    fun default(): BrushColor = byId.getValue(DEFAULT_BRUSH_ID)
}
