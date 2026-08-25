package com.example.cozypuzzle.presentation.cat.select.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.common.formatDuration
import com.example.cozypuzzle.databinding.ItemCatLevelBinding
import com.example.cozypuzzle.presentation.cat.select.CatLevelListItemUiModel

class CatLevelAdapter(
    private val onStart: (levelId: String) -> Unit
) : ListAdapter<CatLevelListItemUiModel, CatLevelAdapter.CatLevelViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatLevelViewHolder =
        CatLevelViewHolder(
            ItemCatLevelBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onStart
        )

    override fun onBindViewHolder(holder: CatLevelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CatLevelViewHolder(
        private val binding: ItemCatLevelBinding,
        private val onStart: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CatLevelListItemUiModel) {
            val context = binding.root.context
            binding.levelTitle.text = item.title
            binding.levelDifficulty.setText(item.difficultyLabelRes)
            binding.levelSize.text = context.getString(R.string.grid_size_format, item.gridSize)
            binding.levelBest.text = item.bestTimeMs
                ?.let { context.getString(R.string.best_time_format, it.formatDuration()) }
                ?: context.getString(R.string.best_time_none)
            binding.btnPlayLevel.setOnClickListener { onStart(item.id) }
        }
    }

    private companion object {

        val DIFF = object : DiffUtil.ItemCallback<CatLevelListItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: CatLevelListItemUiModel,
                newItem: CatLevelListItemUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: CatLevelListItemUiModel,
                newItem: CatLevelListItemUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
