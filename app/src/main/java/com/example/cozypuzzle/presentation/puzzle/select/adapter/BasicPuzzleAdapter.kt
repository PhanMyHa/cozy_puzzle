package com.example.cozypuzzle.presentation.puzzle.select.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.common.formatDuration
import com.example.cozypuzzle.databinding.ItemBasicPuzzleBinding
import com.example.cozypuzzle.presentation.puzzle.select.BasicPuzzleListItemUiModel
import com.google.android.material.button.MaterialButton

class BasicPuzzleAdapter(
    private val onStartWithSize: (puzzleId: String, gridSize: Int) -> Unit
) : ListAdapter<BasicPuzzleListItemUiModel, BasicPuzzleAdapter.PuzzleViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PuzzleViewHolder =
        PuzzleViewHolder(
            ItemBasicPuzzleBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onStartWithSize
        )

    override fun onBindViewHolder(holder: PuzzleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PuzzleViewHolder(
        private val binding: ItemBasicPuzzleBinding,
        private val onStartWithSize: (String, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BasicPuzzleListItemUiModel) {
            val context = binding.root.context
            binding.puzzleImage.setImageResource(item.imageRes)
            binding.puzzleTitle.text = item.title
            binding.puzzleDifficulty.setText(item.difficultyLabelRes)
            binding.puzzleBest.text = item.bestTimeMs
                ?.let { context.getString(R.string.best_time_format, it.formatDuration()) }
                ?: context.getString(R.string.best_time_none)

            val layoutInflater = LayoutInflater.from(context)
            binding.sizeChips.removeAllViews()
            item.gridSizes.forEach { size ->
                val chip = layoutInflater
                    .inflate(R.layout.item_grid_size_chip, binding.sizeChips, false) as MaterialButton
                chip.text = context.getString(R.string.grid_size_format, size)
                chip.setOnClickListener { onStartWithSize(item.id, size) }
                binding.sizeChips.addView(chip)
            }
        }
    }

    private companion object {

        val DIFF = object : DiffUtil.ItemCallback<BasicPuzzleListItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: BasicPuzzleListItemUiModel,
                newItem: BasicPuzzleListItemUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: BasicPuzzleListItemUiModel,
                newItem: BasicPuzzleListItemUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
