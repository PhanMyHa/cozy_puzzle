package com.example.cozypuzzle.presentation.profile.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemAchievementBinding
import com.example.cozypuzzle.domain.model.profile.AchievementUiModel

class AchievementAdapter :
    ListAdapter<AchievementUiModel, AchievementAdapter.AchievementViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AchievementViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AchievementUiModel) {
            val context = binding.root.context

            binding.ivAchIcon.setImageResource(item.iconRes)
            binding.tvAchTitle.setText(item.titleRes)
            binding.tvAchDesc.setText(item.descRes)

            binding.tvAchProgress.text = "${item.currentProgress}/${item.target}"

            val percent = if (item.target > 0) {
                ((item.currentProgress.toFloat() / item.target) * 100).toInt().coerceIn(0, 100)
            } else 0
            binding.pbAchProgress.progress = percent

            if (item.isUnlocked) {
                binding.tvAchStatus.setText(R.string.profile_achievement_unlocked)
                binding.tvAchStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.cozy_rarity_common)
                )
                binding.tvAchStatus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.cozy_rarity_common_bg)
                )
            } else {
                binding.tvAchStatus.setText(R.string.profile_achievement_locked)
                binding.tvAchStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.cozy_subtext)
                )
                binding.tvAchStatus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.cozy_dot_inactive)
                )
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<AchievementUiModel>() {
        override fun areItemsTheSame(
            oldItem: AchievementUiModel,
            newItem: AchievementUiModel
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: AchievementUiModel,
            newItem: AchievementUiModel
        ): Boolean = oldItem == newItem
    }
}
