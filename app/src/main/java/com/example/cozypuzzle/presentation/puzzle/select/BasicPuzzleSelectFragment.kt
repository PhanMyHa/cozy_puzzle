package com.example.cozypuzzle.presentation.puzzle.select

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
import com.example.cozypuzzle.core.common.formatDuration
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentBasicPuzzleSelectBinding
import com.example.cozypuzzle.presentation.puzzle.PuzzleNavArgs
import com.example.cozypuzzle.presentation.puzzle.select.adapter.BasicPuzzleAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BasicPuzzleSelectFragment :
    BaseFragment<FragmentBasicPuzzleSelectBinding>(FragmentBasicPuzzleSelectBinding::inflate) {

    private val viewModel: BasicPuzzleSelectViewModel by viewModels()

    private val adapter = BasicPuzzleAdapter(
        onStartWithSize = { puzzleId, gridSize ->
            viewModel.onEvent(BasicPuzzleSelectEvent.StartPuzzle(puzzleId, gridSize))
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupList() {
        binding.puzzlesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.puzzlesRecycler.adapter = adapter
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

    private fun render(state: BasicPuzzleSelectUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.emptyText.isVisible = !state.isLoading && state.puzzles.isEmpty()
        adapter.submitList(state.puzzles)
    }

    private fun handleEffect(effect: BasicPuzzleSelectEffect) {
        when (effect) {
            is BasicPuzzleSelectEffect.NavigateToGameplay -> {
                val args = bundleOf(
                    PuzzleNavArgs.KEY_PUZZLE_ID to effect.puzzleId,
                    PuzzleNavArgs.KEY_GRID_SIZE to effect.gridSize
                )
                findNavController().navigate(R.id.puzzleFragment, args)
            }
        }
    }
}
