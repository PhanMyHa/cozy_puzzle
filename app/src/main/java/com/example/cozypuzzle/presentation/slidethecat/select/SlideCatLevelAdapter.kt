package com.example.cozypuzzle.presentation.slidethecat.select

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemCozyLevelBinding

class SlideCatLevelAdapter(
    private val onLevelClick: (SlideCatLevelUiItem) -> Unit
) : ListAdapter<SlideCatLevelUiItem, SlideCatLevelAdapter.LevelViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val binding = ItemCozyLevelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LevelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LevelViewHolder(
        private val binding: ItemCozyLevelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SlideCatLevelUiItem) {
            binding.tvLevelNumber.text = item.level.number.toString()
            binding.ivLevelThumb.setImageResource(item.level.thumbnailRes)

            if (item.isUnlocked) {
                binding.layoutLockedOverlay.visibility = View.GONE
                binding.layoutStars.visibility = View.VISIBLE
                binding.cardLevel.alpha = 1.0f

                binding.ivStar1.setImageResource(
                    if (item.stars >= 1) R.drawable.ic_star_yellow else R.drawable.ic_star_empty
                )
                binding.ivStar2.setImageResource(
                    if (item.stars >= 2) R.drawable.ic_star_yellow else R.drawable.ic_star_empty
                )
                binding.ivStar3.setImageResource(
                    if (item.stars >= 3) R.drawable.ic_star_yellow else R.drawable.ic_star_empty
                )
            } else {
                binding.layoutLockedOverlay.visibility = View.VISIBLE
                binding.layoutStars.visibility = View.INVISIBLE
                binding.cardLevel.alpha = 0.65f
            }

            binding.cardLevel.setOnClickListener {
                onLevelClick(item)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<SlideCatLevelUiItem>() {
        override fun areItemsTheSame(oldItem: SlideCatLevelUiItem, newItem: SlideCatLevelUiItem): Boolean =
            oldItem.level.id == newItem.level.id

        override fun areContentsTheSame(oldItem: SlideCatLevelUiItem, newItem: SlideCatLevelUiItem): Boolean =
            oldItem == newItem
    }
}
