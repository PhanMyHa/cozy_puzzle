package com.example.cozypuzzle.presentation.cat.select

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentCatLevelSelectBinding
import com.example.cozypuzzle.presentation.cat.select.adapter.CatLevelAdapter
import com.example.cozypuzzle.presentation.puzzle.PuzzleNavArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CatLevelSelectFragment :
    BaseFragment<FragmentCatLevelSelectBinding>(FragmentCatLevelSelectBinding::inflate) {

    private val viewModel: CatLevelSelectViewModel by viewModels()

    private val adapter = CatLevelAdapter(
        onStart = { levelId -> viewModel.onEvent(CatLevelSelectEvent.StartLevel(levelId)) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupList() {
        binding.levelsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.levelsRecycler.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refresh()
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

    private fun render(state: CatLevelSelectUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.emptyText.isVisible = !state.isLoading && state.levels.isEmpty()
        adapter.submitList(state.levels)
    }

    private fun handleEffect(effect: CatLevelSelectEffect) {
        when (effect) {
            is CatLevelSelectEffect.NavigateToGameplay -> {
                val args = bundleOf(PuzzleNavArgs.KEY_LEVEL_ID to effect.levelId)
                findNavController().navigate(R.id.catGameFragment, args)
            }
        }
    }
}
