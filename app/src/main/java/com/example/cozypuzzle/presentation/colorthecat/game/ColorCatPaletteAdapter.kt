package com.example.cozypuzzle.presentation.colorthecat.game

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.databinding.ItemColorPaletteBinding

class ColorCatPaletteAdapter(
    private val onColorClick: (Int) -> Unit
) : RecyclerView.Adapter<ColorCatPaletteAdapter.PaletteViewHolder>() {

    private var colors: List<Int> = emptyList()
    private var selectedColor: Int = 0

    fun submitColors(newColors: List<Int>, currentSelected: Int) {
        colors = newColors
        selectedColor = currentSelected
        notifyDataSetChanged()
    }

    fun setSelectedColor(color: Int) {
        if (selectedColor != color) {
            selectedColor = color
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaletteViewHolder {
        val binding = ItemColorPaletteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaletteViewHolder(binding)
    }

    override fun getItemCount(): Int = colors.size

    override fun onBindViewHolder(holder: PaletteViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    inner class PaletteViewHolder(
        private val binding: ItemColorPaletteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(@ColorInt color: Int) {
            val isSelected = color == selectedColor

            // Create circular swatch drawable
            val swatchDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                // If white or very light color, add light stroke
                if (color == Color.WHITE || Color.alpha(color) == 0) {
                    setStroke(2, 0xFFD8EEF8.toInt())
                }
            }
            binding.viewColorSwatch.background = swatchDrawable

            // Outer selection ring
            if (isSelected) {
                binding.viewSelectionRing.visibility = View.VISIBLE
                val ringDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0x00000000)
                    setStroke(5, 0xFFFFB6D2.toInt()) // Cozy pastel pink ring
                }
                binding.viewSelectionRing.background = ringDrawable
                binding.root.scaleX = 1.08f
                binding.root.scaleY = 1.08f
            } else {
                binding.viewSelectionRing.visibility = View.GONE
                binding.root.scaleX = 1.0f
                binding.root.scaleY = 1.0f
            }

            binding.root.setOnClickListener {
                onColorClick(color)
            }
        }
    }
}
