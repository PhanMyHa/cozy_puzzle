package com.example.cozypuzzle.presentation.findpair.select

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
import com.example.cozypuzzle.core.findpair.model.FindPairDifficulty
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.FragmentFindPairLevelSelectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FindPairLevelSelectFragment :
    BaseFragment<FragmentFindPairLevelSelectBinding>(FragmentFindPairLevelSelectBinding::inflate) {

    private val viewModel: FindPairLevelSelectViewModel by viewModels()
    private lateinit var adapter: FindPairLevelAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeUiState()
        observeEffects()
    }

    private fun setupViews() {
        adapter = FindPairLevelAdapter { item ->
            viewModel.onLevelClicked(item)
        }
        binding.recyclerLevels.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerLevels.adapter = adapter

        binding.btnBack.setOnClickListener {
            viewModel.onBackClicked()
        }

        binding.tabEasy.setOnClickListener {
            viewModel.selectDifficulty(FindPairDifficulty.EASY)
        }
        binding.tabMedium.setOnClickListener {
            viewModel.selectDifficulty(FindPairDifficulty.MEDIUM)
        }
        binding.tabHard.setOnClickListener {
            viewModel.selectDifficulty(FindPairDifficulty.HARD)
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

    private fun updateTabStyles(difficulty: FindPairDifficulty) {
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val subtext = ContextCompat.getColor(requireContext(), R.color.cozy_subtext)

        binding.tabEasy.apply {
            setBackgroundResource(if (difficulty == FindPairDifficulty.EASY) R.drawable.bg_tab_pill_selected else R.drawable.bg_tab_pill_unselected)
            setTextColor(if (difficulty == FindPairDifficulty.EASY) white else subtext)
        }
        binding.tabMedium.apply {
            setBackgroundResource(if (difficulty == FindPairDifficulty.MEDIUM) R.drawable.bg_tab_pill_selected else R.drawable.bg_tab_pill_unselected)
            setTextColor(if (difficulty == FindPairDifficulty.MEDIUM) white else subtext)
        }
        binding.tabHard.apply {
            setBackgroundResource(if (difficulty == FindPairDifficulty.HARD) R.drawable.bg_tab_pill_selected else R.drawable.bg_tab_pill_unselected)
            setTextColor(if (difficulty == FindPairDifficulty.HARD) white else subtext)
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is FindPairLevelSelectEffect.NavigateToGame -> {
                            val bundle = bundleOf(KEY_LEVEL_ID to effect.levelId)
                            findNavController().navigate(
                                R.id.action_find_pair_select_to_game,
                                bundle
                            )
                        }
                        is FindPairLevelSelectEffect.ShowLockedDialog -> {
                            showLockedDialog(effect.levelNumber)
                        }
                        FindPairLevelSelectEffect.NavigateBack -> {
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
            message = getString(R.string.find_pair_locked_message)
        )
    }

    companion object {
        const val KEY_LEVEL_ID = "level_id"
    }
}
