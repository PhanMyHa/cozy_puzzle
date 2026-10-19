package com.example.cozypuzzle.presentation.colorthecat.select

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemCozyLevelBinding

class ColorCatLevelAdapter(
    private val onLevelClick: (ColorCatLevelUiItem) -> Unit
) : ListAdapter<ColorCatLevelUiItem, ColorCatLevelAdapter.LevelViewHolder>(DiffCallback) {

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

        fun bind(item: ColorCatLevelUiItem) {
            binding.tvLevelNumber.text = item.level.number.toString()

            val thumbRes = item.level.thumbnailRes ?: R.drawable.img_puzzle_cute_cat
            binding.ivLevelThumb.setImageResource(thumbRes)

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

    private object DiffCallback : DiffUtil.ItemCallback<ColorCatLevelUiItem>() {
        override fun areItemsTheSame(oldItem: ColorCatLevelUiItem, newItem: ColorCatLevelUiItem): Boolean =
            oldItem.level.id == newItem.level.id

        override fun areContentsTheSame(oldItem: ColorCatLevelUiItem, newItem: ColorCatLevelUiItem): Boolean =
            oldItem == newItem
    }
}
