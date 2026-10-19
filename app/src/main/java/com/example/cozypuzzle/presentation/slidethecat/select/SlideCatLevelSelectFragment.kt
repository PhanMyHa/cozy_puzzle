package com.example.cozypuzzle.presentation.slidethecat.select

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.FragmentSlideCatLevelSelectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SlideCatLevelSelectFragment :
    BaseFragment<FragmentSlideCatLevelSelectBinding>(FragmentSlideCatLevelSelectBinding::inflate) {

    private val viewModel: SlideCatLevelSelectViewModel by viewModels()
    private lateinit var adapter: SlideCatLevelAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeUiState()
        observeEffects()
    }

    private fun setupViews() {
        adapter = SlideCatLevelAdapter { item ->
            viewModel.onLevelClicked(item)
        }
        binding.recyclerLevels.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerLevels.adapter = adapter

        binding.btnBack.setOnClickListener {
            viewModel.onBackClicked()
        }

        binding.tabEasy.setOnClickListener {
            viewModel.selectDifficulty(SlideCatDifficulty.EASY)
        }
        binding.tabMedium.setOnClickListener {
            viewModel.selectDifficulty(SlideCatDifficulty.MEDIUM)
        }
        binding.tabHard.setOnClickListener {
            viewModel.selectDifficulty(SlideCatDifficulty.HARD)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvTotalStars.text = state.totalStars.toString()
                    adapter.submitList(state.levels)
                    updateTabStyles(state.selectedDifficulty)
                }
            }
        }
    }

    private fun updateTabStyles(difficulty: SlideCatDifficulty) {
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val subtext = ContextCompat.getColor(requireContext(), R.color.cozy_subtext)

        binding.tabEasy.apply {
            setBackgroundResource(if (difficulty == SlideCatDifficulty.EASY) R.drawable.bg_tab_pill_selected else R.drawable.bg_tab_pill_unselected)
            setTextColor(if (difficulty == SlideCatDifficulty.EASY) white else subtext)
        }
        binding.tabMedium.apply {
            setBackgroundResource(if (difficulty == SlideCatDifficulty.MEDIUM) R.drawable.bg_tab_pill_selected else R.drawable.bg_tab_pill_unselected)
            setTextColor(if (difficulty == SlideCatDifficulty.MEDIUM) white else subtext)
        }
        binding.tabHard.apply {
            setBackgroundResource(if (difficulty == SlideCatDifficulty.HARD) R.drawable.bg_tab_pill_selected else R.drawable.bg_tab_pill_unselected)
            setTextColor(if (difficulty == SlideCatDifficulty.HARD) white else subtext)
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is SlideCatLevelSelectEffect.NavigateToGame -> {
                            val bundle = bundleOf(KEY_LEVEL_ID to effect.levelId)
                            findNavController().navigate(
                                R.id.action_slide_cat_select_to_game,
                                bundle
                            )
                        }
                        is SlideCatLevelSelectEffect.ShowLockedDialog -> {
                            showLockedDialog(effect.levelNumber)
                        }
                        SlideCatLevelSelectEffect.NavigateBack -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun showLockedDialog(levelNumber: Int) {
        CozyDialogs.showLockedDialog(
            context = requireContext(),
            levelNumber = levelNumber,
            message = getString(R.string.slide_cat_locked_message)
        )
    }

    companion object {
        const val KEY_LEVEL_ID = "level_id"
    }
}
