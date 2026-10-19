package com.example.cozypuzzle.presentation.catpuzzle.select

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.FragmentCatPuzzleLevelSelectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CatPuzzleLevelSelectFragment :
    BaseFragment<FragmentCatPuzzleLevelSelectBinding>(FragmentCatPuzzleLevelSelectBinding::inflate) {

    private val viewModel: CatPuzzleLevelSelectViewModel by viewModels()

    private val adapter = CatPuzzleLevelAdapter(
        onLevelClicked = { levelId -> viewModel.onLevelClicked(levelId) },
        onLockedClicked = { item -> viewModel.onLockedLevelClicked(item.number) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupList() {
        binding.levelsRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.levelsRecycler.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.onBackClicked()
        }
        binding.btnTabIntermediate.setOnClickListener {
            viewModel.onLockedLevelClicked(1)
        }
        binding.btnTabHard.setOnClickListener {
            viewModel.onLockedLevelClicked(1)
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

    private fun render(state: CatPuzzleLevelSelectUiState) {
        binding.tvStarCount.text = state.playerStars.toString()
        adapter.submitList(state.levels)
    }

    private fun handleEffect(effect: CatPuzzleSelectEffect) {
        when (effect) {
            is CatPuzzleSelectEffect.NavigateToGame -> {
                val args = bundleOf(KEY_LEVEL_ID to effect.levelId)
                findNavController().navigate(R.id.action_cat_puzzle_select_to_game, args)
            }
            is CatPuzzleSelectEffect.ShowLockedDialog -> {
                CozyDialogs.showLockedDialog(
                    context = requireContext(),
                    levelNumber = effect.levelNumber,
                    message = getString(R.string.cat_puzzle_level_locked_hint)
                )
            }
            CatPuzzleSelectEffect.NavigateBack -> {
                findNavController().popBackStack()
            }
        }
    }

    companion object {
        const val KEY_LEVEL_ID = "cat_puzzle_level_id"
    }
}
