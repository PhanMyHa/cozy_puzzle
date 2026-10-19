package com.example.cozypuzzle.presentation.starshop.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemStarShopAchievementBinding
import com.example.cozypuzzle.domain.model.starshop.StarRewardStatus
import com.example.cozypuzzle.domain.model.starshop.StarShopAchievementItem

class StarShopAchievementAdapter(
    private val onClaimClicked: (String) -> Unit
) : ListAdapter<StarShopAchievementItem, StarShopAchievementAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStarShopAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemStarShopAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StarShopAchievementItem) {
            val context = binding.root.context
            binding.ivAchIcon.setImageResource(item.iconRes)
            binding.tvAchTitle.setText(item.titleRes)
            binding.tvAchDesc.setText(item.descRes)

            binding.pbAchProgress.max = item.target
            binding.pbAchProgress.progress = item.currentProgress
            binding.tvAchProgress.text = "${item.currentProgress}/${item.target}"

            binding.tvAchReward.text = "+${item.rewardStars} ⭐"

            when (item.status) {
                StarRewardStatus.CLAIMABLE -> {
                    binding.btnAchAction.isEnabled = true
                    binding.btnAchAction.text = context.getString(R.string.star_shop_btn_claim, item.rewardStars)
                    binding.btnAchAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.cozy_pastel_pink)
                    )
                    binding.btnAchAction.setTextColor(ContextCompat.getColor(context, R.color.white))
                    binding.btnAchAction.setOnClickListener {
                        onClaimClicked(item.id)
                    }
                }
                StarRewardStatus.CLAIMED -> {
                    binding.btnAchAction.isEnabled = false
                    binding.btnAchAction.text = context.getString(R.string.star_shop_btn_claimed)
                    binding.btnAchAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.cozy_tab_unselected)
                    )
                    binding.btnAchAction.setTextColor(ContextCompat.getColor(context, R.color.cozy_subtext))
                    binding.btnAchAction.setOnClickListener(null)
                }
                StarRewardStatus.NOT_COMPLETED -> {
                    binding.btnAchAction.isEnabled = false
                    binding.btnAchAction.text = "${item.currentProgress}/${item.target}"
                    binding.btnAchAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.cozy_tab_unselected)
                    )
                    binding.btnAchAction.setTextColor(ContextCompat.getColor(context, R.color.cozy_subtext))
                    binding.btnAchAction.setOnClickListener(null)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StarShopAchievementItem>() {
        override fun areItemsTheSame(
            oldItem: StarShopAchievementItem,
            newItem: StarShopAchievementItem
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: StarShopAchievementItem,
            newItem: StarShopAchievementItem
        ): Boolean = oldItem == newItem
    }
}
