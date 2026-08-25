package com.example.cozypuzzle.presentation.cat

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.common.formatDuration
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentCatGameBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CatGameFragment : BaseFragment<FragmentCatGameBinding>(FragmentCatGameBinding::inflate) {

    private val viewModel: CatGameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBoard()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupBoard() {
        binding.catBoard.onCellTapped = { cell ->
            viewModel.onEvent(CatGameEvent.CellTapped(cell))
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { viewModel.onEvent(CatGameEvent.Back) }
        binding.btnPause.setOnClickListener { viewModel.onEvent(CatGameEvent.Pause) }
        binding.btnUndo.setOnClickListener { viewModel.onEvent(CatGameEvent.Undo) }
        binding.btnRedo.setOnClickListener { viewModel.onEvent(CatGameEvent.Redo) }
        binding.btnHint.setOnClickListener { viewModel.onEvent(CatGameEvent.Hint) }
        binding.btnRestart.setOnClickListener { viewModel.onEvent(CatGameEvent.Restart) }
        binding.btnResume.setOnClickListener { viewModel.onEvent(CatGameEvent.Resume) }
        binding.btnPauseRestart.setOnClickListener { viewModel.onEvent(CatGameEvent.Restart) }
        binding.btnPauseQuit.setOnClickListener { viewModel.onEvent(CatGameEvent.Back) }
        binding.walkButton.setOnClickListener { viewModel.onEvent(CatGameEvent.Walk) }
        binding.btnRetry.setOnClickListener { viewModel.onEvent(CatGameEvent.Restart) }
        binding.btnChooseAnother.setOnClickListener { findNavController().popBackStack() }
        binding.btnResultHome.setOnClickListener { navigateHome() }
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

    private fun render(state: CatGameUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.errorText.isVisible = state.isError
        if (state.isLoading || state.isError) return

        val playing = !state.isCompleted
        binding.catBoard.isVisible = playing
        binding.controlsRow.isVisible = playing && !state.isWalking
        binding.walkButton.isVisible = playing && !state.isWalking
        binding.btnPause.isVisible = playing && !state.isWalking
        binding.pauseOverlay.isVisible = state.isPaused
        binding.completionOverlay.isVisible = state.isCompleted

        binding.levelTitle.text = state.levelTitle
        binding.catBoard.render(
            board = state.board,
            selectedCell = state.selectedCell,
            hintCells = state.hintCells,
            pathCells = state.pathCells,
            walkIndex = state.walkIndex
        )

        binding.hudTimer.text =
            getString(R.string.hud_timer_format, state.elapsedTimeMs.formatDuration())
        binding.hudMoves.text = getString(R.string.hud_moves_format, state.moves)
        binding.hudHints.text = getString(R.string.hud_hints_format, state.hintsRemaining)

        binding.pathStatus.setText(
            when {
                state.pathFound -> R.string.cat_path_ready
                else -> R.string.cat_path_missing
            }
        )
        binding.pathStatus.setTextColor(
            if (state.pathFound) {
                ContextCompat.getColor(requireContext(), R.color.cozy_green_dark)
            } else {
                ContextCompat.getColor(requireContext(), R.color.cozy_blossom_dark)
            }
        )

        val selectedCell = state.selectedCell
        binding.btnRotate.isEnabled = selectedCell != null &&
            state.board?.tileAt(selectedCell) != null &&
            selectedCell != state.board?.catPosition &&
            selectedCell != state.board?.homePosition
        binding.btnUndo.isEnabled = state.canUndo
        binding.btnRedo.isEnabled = state.canRedo
        binding.btnHint.isEnabled = state.hintsRemaining > 0 && !state.pathFound
        binding.walkButton.isEnabled = state.pathFound

        if (state.isCompleted) renderCompletion(state)
    }

    private fun renderCompletion(state: CatGameUiState) {
        val result = state.result ?: return
        binding.resultLevelName.text = state.levelTitle
        binding.resultTime.text =
            getString(R.string.result_time_format, result.durationMs.formatDuration())
        binding.resultMoves.text = getString(R.string.result_moves_format, result.moveCount)
        binding.resultHints.text =
            if (result.isPerfect) {
                getString(R.string.result_perfect)
            } else {
                getString(R.string.result_hints_format, result.hintsUsed)
            }
    }

    private fun handleEffect(effect: CatGameEffect) {
        when (effect) {
            CatGameEffect.NavigateBackToSelection -> findNavController().popBackStack()
            CatGameEffect.NavigateHome -> navigateHome()
        }
    }

    private fun navigateHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }
}
