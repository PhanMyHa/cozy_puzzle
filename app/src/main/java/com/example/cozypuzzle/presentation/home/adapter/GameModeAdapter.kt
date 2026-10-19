package com.example.cozypuzzle.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.core.puzzle.model.PuzzleMode
import com.example.cozypuzzle.databinding.ItemGameModeBinding
import com.example.cozypuzzle.presentation.home.GameModeUiModel

class GameModeAdapter(
    private val onClick: (PuzzleMode) -> Unit
) : ListAdapter<GameModeUiModel, GameModeAdapter.GameModeViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameModeViewHolder {
        val binding = ItemGameModeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GameModeViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: GameModeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GameModeViewHolder(
        private val binding: ItemGameModeBinding,
        private val onClick: (PuzzleMode) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GameModeUiModel) {
            binding.gameModeTitle.setText(item.titleRes)
            binding.gameModeThumbnail.setImageResource(item.thumbnailRes)
            binding.root.setOnClickListener { onClick(item.mode) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GameModeUiModel>() {
            override fun areItemsTheSame(
                oldItem: GameModeUiModel,
                newItem: GameModeUiModel
            ): Boolean = oldItem.mode == newItem.mode

            override fun areContentsTheSame(
                oldItem: GameModeUiModel,
                newItem: GameModeUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
