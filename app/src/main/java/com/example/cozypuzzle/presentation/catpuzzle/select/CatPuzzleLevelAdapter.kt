package com.example.cozypuzzle.presentation.catpuzzle.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemCozyLevelBinding

class CatPuzzleLevelAdapter(
    private val onLevelClicked: (levelId: String) -> Unit,
    private val onLockedClicked: (item: CatPuzzleLevelItemUiModel) -> Unit
) : ListAdapter<CatPuzzleLevelItemUiModel, CatPuzzleLevelAdapter.LevelViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val binding = ItemCozyLevelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LevelViewHolder(binding, onLevelClicked, onLockedClicked)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LevelViewHolder(
        private val binding: ItemCozyLevelBinding,
        private val onLevelClicked: (String) -> Unit,
        private val onLockedClicked: (CatPuzzleLevelItemUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CatPuzzleLevelItemUiModel) {
            binding.tvLevelNumber.text = item.number.toString()
            binding.ivLevelThumb.setImageResource(item.imageRes)
            binding.layoutLockedOverlay.isVisible = !item.isUnlocked

            if (item.isUnlocked) {
                binding.cardLevel.alpha = 1.0f
                binding.layoutStars.isVisible = true
                binding.ivStar1.setImageResource(if (item.stars >= 1) R.drawable.ic_star_yellow else R.drawable.ic_star_empty)
                binding.ivStar2.setImageResource(if (item.stars >= 2) R.drawable.ic_star_yellow else R.drawable.ic_star_empty)
                binding.ivStar3.setImageResource(if (item.stars >= 3) R.drawable.ic_star_yellow else R.drawable.ic_star_empty)

                binding.cardLevel.setOnClickListener { onLevelClicked(item.id) }
            } else {
                binding.cardLevel.alpha = 0.65f
                binding.layoutStars.isVisible = false
                binding.cardLevel.setOnClickListener { onLockedClicked(item) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CatPuzzleLevelItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: CatPuzzleLevelItemUiModel,
                newItem: CatPuzzleLevelItemUiModel
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: CatPuzzleLevelItemUiModel,
                newItem: CatPuzzleLevelItemUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
