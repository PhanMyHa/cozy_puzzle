package com.example.cozypuzzle.presentation.starshop

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.BottomSheetStarShopBinding
import com.example.cozypuzzle.domain.model.starshop.StarShopState
import com.example.cozypuzzle.presentation.starshop.adapter.StarShopAchievementAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StarShopBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetStarShopBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StarShopViewModel by viewModels()

    private val achievementAdapter = StarShopAchievementAdapter { achievementId ->
        viewModel.onClaimAchievementClicked(achievementId)
    }

    override fun getTheme(): Int = R.style.Theme_CozyPuzzle_BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetStarShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAchievementsRecycler()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupAchievementsRecycler() {
        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAchievements.adapter = achievementAdapter
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            viewModel.onCloseClicked()
        }
        binding.btnDailyLoginClaim.setOnClickListener {
            viewModel.onClaimDailyLoginClicked()
        }
        binding.btnLevelsPlay.setOnClickListener {
            viewModel.onPlayLevelsClicked()
        }
        binding.btnDailyMissionsGo.setOnClickListener {
            viewModel.onGoToDailyClicked()
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

    private fun render(state: StarShopState) {
        val context = requireContext()

        // 1. Current Star Balance
        binding.tvCurrentStars.text = getString(R.string.star_shop_balance_format, state.currentStars)

        // 2. Daily Login
        if (state.dailyLoginClaimedToday) {
            binding.tvDailyLoginStatus.text = getString(R.string.star_shop_daily_login_claimed_today)
            binding.btnDailyLoginClaim.text = getString(R.string.star_shop_status_claimed)
            binding.btnDailyLoginClaim.isEnabled = false
            binding.btnDailyLoginClaim.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.cozy_tab_unselected)
            )
            binding.btnDailyLoginClaim.setTextColor(
                ContextCompat.getColor(context, R.color.cozy_subtext)
            )
        } else {
            binding.tvDailyLoginStatus.text = getString(R.string.star_shop_daily_login_ready)
            binding.btnDailyLoginClaim.text = getString(R.string.star_shop_btn_claim, state.dailyLoginReward)
            binding.btnDailyLoginClaim.isEnabled = true
            binding.btnDailyLoginClaim.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.cozy_pastel_pink)
            )
            binding.btnDailyLoginClaim.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
        }

        // 3. Complete Levels
        val levelsText = getString(
            R.string.star_shop_levels_progress_format,
            state.levelsCompletedCount,
            state.totalLevelsCount
        ) + " • " + getString(R.string.star_shop_levels_reward)
        binding.tvLevelsProgress.text = levelsText

        // 4. Daily Missions
        val missionsText = getString(
            R.string.star_shop_daily_missions_progress_format,
            state.dailyMissionsCompletedCount,
            state.dailyMissionsTotalCount
        ) + " • " + getString(R.string.star_shop_daily_missions_reward)
        binding.tvDailyMissionsProgress.text = missionsText

        // 5. Achievements
        binding.tvAchievementsHeader.text = getString(
            R.string.star_shop_achievements_header_format,
            state.unlockedAchievementsCount,
            state.totalAchievementsCount
        )
        achievementAdapter.submitList(state.achievements)
    }

    private fun handleEffect(effect: StarShopEffect) {
        when (effect) {
            is StarShopEffect.ShowRewardCelebration -> {
                CozyDialogs.showRewardClaimedDialog(
                    context = requireContext(),
                    amount = effect.amount,
                    title = effect.title
                )
            }
            StarShopEffect.NavigateToGames -> {
                dismiss()
                val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav?.selectedItemId = R.id.homeFragment
            }
            StarShopEffect.NavigateToDaily -> {
                dismiss()
                val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
                bottomNav?.selectedItemId = R.id.dailyTasksFragment
            }
            StarShopEffect.Dismiss -> {
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "StarShopBottomSheetFragment"

        fun newInstance(): StarShopBottomSheetFragment = StarShopBottomSheetFragment()
    }
}
