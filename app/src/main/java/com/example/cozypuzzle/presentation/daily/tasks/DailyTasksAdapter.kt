package com.example.cozypuzzle.presentation.daily.tasks

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cozypuzzle.R
import com.example.cozypuzzle.databinding.ItemDailyMissionBinding
import com.example.cozypuzzle.domain.model.daily.DailyMissionItemUiModel

class DailyTasksAdapter(
    private val onMissionActionClicked: (DailyMissionItemUiModel) -> Unit
) : ListAdapter<DailyMissionItemUiModel, DailyTasksAdapter.MissionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val binding = ItemDailyMissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MissionViewHolder(binding, onMissionActionClicked)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MissionViewHolder(
        private val binding: ItemDailyMissionBinding,
        private val onActionClicked: (DailyMissionItemUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyMissionItemUiModel) {
            val context = binding.root.context

            binding.ivMissionIcon.setImageResource(item.iconRes)
            binding.tvMissionTitle.setText(item.titleRes)
            binding.tvMissionDesc.setText(item.descRes)

            binding.tvMissionReward.text = context.getString(
                R.string.daily_tasks_reward_format,
                item.rewardCoins
            )

            binding.tvMissionProgress.text = context.getString(
                R.string.daily_tasks_mission_progress_format,
                item.currentProgress,
                item.target
            )

            val percent = if (item.target > 0) {
                ((item.currentProgress.toFloat() / item.target) * 100).toInt().coerceIn(0, 100)
            } else 0
            binding.pbMissionProgress.progress = percent

            when {
                item.isClaimed -> {
                    binding.btnMissionAction.visibility = View.GONE
                    binding.tvMissionClaimed.visibility = View.VISIBLE
                }
                item.isCompleted -> {
                    binding.tvMissionClaimed.visibility = View.GONE
                    binding.btnMissionAction.visibility = View.VISIBLE
                    binding.btnMissionAction.isEnabled = true
                    binding.btnMissionAction.setText(R.string.daily_tasks_btn_claim)
                    binding.btnMissionAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.cozy_pastel_pink)
                    )
                    binding.btnMissionAction.setOnClickListener { onActionClicked(item) }
                }
                item.targetModeId != null -> {
                    binding.tvMissionClaimed.visibility = View.GONE
                    binding.btnMissionAction.visibility = View.VISIBLE
                    binding.btnMissionAction.isEnabled = true
                    binding.btnMissionAction.setText(R.string.daily_tasks_btn_go)
                    binding.btnMissionAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.cozy_pastel_blue)
                    )
                    binding.btnMissionAction.setOnClickListener { onActionClicked(item) }
                }
                else -> {
                    binding.tvMissionClaimed.visibility = View.GONE
                    binding.btnMissionAction.visibility = View.VISIBLE
                    binding.btnMissionAction.isEnabled = false
                    binding.btnMissionAction.text = context.getString(
                        R.string.daily_tasks_mission_progress_format,
                        item.currentProgress,
                        item.target
                    )
                    binding.btnMissionAction.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.cozy_dot_inactive)
                    )
                    binding.btnMissionAction.setOnClickListener(null)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<DailyMissionItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: DailyMissionItemUiModel,
            newItem: DailyMissionItemUiModel
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DailyMissionItemUiModel,
            newItem: DailyMissionItemUiModel
        ): Boolean = oldItem == newItem
    }
}
