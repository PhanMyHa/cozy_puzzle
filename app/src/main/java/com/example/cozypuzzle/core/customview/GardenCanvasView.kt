package com.example.cozypuzzle.core.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View

class GardenCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onItemSelected: ((String) -> Unit)? = null
    var onItemMoved: ((String, Float, Float) -> Unit)? = null
}
