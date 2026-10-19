package com.example.cozypuzzle.presentation.daily.tasks

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.FragmentDailyTasksBinding
import com.example.cozypuzzle.domain.model.daily.DailyStreakUiModel
import com.example.cozypuzzle.domain.model.daily.DailyTasksEffect
import com.example.cozypuzzle.domain.model.daily.DailyTasksUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DailyTasksFragment :
    BaseFragment<FragmentDailyTasksBinding>(FragmentDailyTasksBinding::inflate) {

    private val viewModel: DailyTasksViewModel by viewModels()
    private lateinit var adapter: DailyTasksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupRecyclerView() {
        adapter = DailyTasksAdapter { mission ->
            viewModel.onMissionActionClicked(mission)
        }
        binding.rvDailyMissions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDailyMissions.adapter = adapter
    }

    private fun setupListeners() {
        binding.layoutCoinsPill.setOnClickListener {
            com.example.cozypuzzle.presentation.starshop.StarShopBottomSheetFragment.newInstance()
                .show(childFragmentManager, com.example.cozypuzzle.presentation.starshop.StarShopBottomSheetFragment.TAG)
        }
        binding.btnClaimBonus.setOnClickListener {
            viewModel.onClaimBonusClicked()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect(::handleEffect)
            }
        }
    }

    private fun render(state: DailyTasksUiState) {
        binding.tvPlayerCoins.text = state.playerCoins.toString()

        renderStreak(state.streak)
        renderBonus(state)
        renderMissions(state)
    }

    private fun renderStreak(streak: DailyStreakUiModel) {
        binding.tvStreakCount.text = getString(
            R.string.daily_tasks_streak_days,
            streak.streakCount
        )

        binding.tvStreakMessage.setText(
            if (streak.isCompletedToday) {
                R.string.daily_tasks_streak_today_done
            } else {
                R.string.daily_tasks_streak_today_todo
            }
        )

        val dayContainers = listOf(
            Triple(binding.tvDayName1, binding.flDayCircle1, Pair(binding.ivDayCheck1, binding.tvDayNum1)),
            Triple(binding.tvDayName2, binding.flDayCircle2, Pair(binding.ivDayCheck2, binding.tvDayNum2)),
            Triple(binding.tvDayName3, binding.flDayCircle3, Pair(binding.ivDayCheck3, binding.tvDayNum3)),
            Triple(binding.tvDayName4, binding.flDayCircle4, Pair(binding.ivDayCheck4, binding.tvDayNum4)),
            Triple(binding.tvDayName5, binding.flDayCircle5, Pair(binding.ivDayCheck5, binding.tvDayNum5)),
            Triple(binding.tvDayName6, binding.flDayCircle6, Pair(binding.ivDayCheck6, binding.tvDayNum6)),
            Triple(binding.tvDayName7, binding.flDayCircle7, Pair(binding.ivDayCheck7, binding.tvDayNum7))
        )

        streak.days.forEachIndexed { index, day ->
            if (index < dayContainers.size) {
                val (tvName, flCircle, checkAndNum) = dayContainers[index]
                val (ivCheck, tvNum) = checkAndNum

                tvName.text = day.dayName
                tvNum.text = day.dayNumber.toString()

                when {
                    day.isCompleted -> {
                        flCircle.setBackgroundResource(R.drawable.bg_day_item_completed)
                        ivCheck.visibility = View.VISIBLE
                        tvNum.visibility = View.GONE
                        tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.cozy_navy_text))
                    }
                    day.isToday -> {
                        flCircle.setBackgroundResource(R.drawable.bg_day_item_today)
                        ivCheck.visibility = View.GONE
                        tvNum.visibility = View.VISIBLE
                        tvNum.setTextColor(ContextCompat.getColor(requireContext(), R.color.cozy_pastel_pink_dark))
                        tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.cozy_pastel_pink_dark))
                    }
                    else -> {
                        flCircle.setBackgroundResource(R.drawable.bg_day_item_normal)
                        ivCheck.visibility = View.GONE
                        tvNum.visibility = View.VISIBLE
                        tvNum.setTextColor(ContextCompat.getColor(requireContext(), R.color.cozy_subtext))
                        tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.cozy_subtext))
                    }
                }
            }
        }
    }

    private fun renderBonus(state: DailyTasksUiState) {
        binding.tvBonusCoins.text = getString(
            R.string.daily_tasks_reward_format,
            state.bonusRewardCoins
        )

        when {
            state.isBonusClaimed -> {
                binding.btnClaimBonus.isEnabled = false
                binding.btnClaimBonus.setText(R.string.daily_tasks_bonus_claimed)
                binding.btnClaimBonus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.cozy_dot_inactive)
                )
            }
            state.isBonusAvailable -> {
                binding.btnClaimBonus.isEnabled = true
                binding.btnClaimBonus.setText(R.string.daily_tasks_bonus_claim)
                binding.btnClaimBonus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.cozy_pastel_pink)
                )
            }
            else -> {
                binding.btnClaimBonus.isEnabled = false
                binding.btnClaimBonus.text = getString(
                    R.string.daily_tasks_bonus_in_progress,
                    state.completedMissionsCount,
                    state.totalMissionsCount
                )
                binding.btnClaimBonus.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.cozy_dot_inactive)
                )
            }
        }
    }

    private fun renderMissions(state: DailyTasksUiState) {
        binding.tvMissionsProgressHeader.text = getString(
            R.string.daily_tasks_bonus_in_progress,
            state.completedMissionsCount,
            state.totalMissionsCount
        )
        adapter.submitList(state.missions)
    }

    private fun handleEffect(effect: DailyTasksEffect) {
        when (effect) {
            is DailyTasksEffect.ShowRewardCelebration -> {
                CozyDialogs.showRewardClaimedDialog(
                    context = requireContext(),
                    amount = effect.amount,
                    title = effect.title
                )
            }
            is DailyTasksEffect.NavigateToGame -> {
                when (effect.targetModeId) {
                    "cat_puzzle" -> findNavController().navigate(R.id.catPuzzleLevelSelectFragment)
                    "color_the_cat" -> findNavController().navigate(R.id.colorCatLevelSelectFragment)
                    "slide_the_cat" -> findNavController().navigate(R.id.slideCatLevelSelectFragment)
                    "find_the_pair" -> findNavController().navigate(R.id.findPairLevelSelectFragment)
                }
            }
            is DailyTasksEffect.ShowMessage -> Unit
        }
    }
}
