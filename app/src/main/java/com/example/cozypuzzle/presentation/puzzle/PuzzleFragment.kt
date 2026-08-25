package com.example.cozypuzzle.presentation.puzzle

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.common.formatDuration
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentPuzzleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PuzzleFragment : BaseFragment<FragmentPuzzleBinding>(FragmentPuzzleBinding::inflate) {

    private val viewModel: PuzzleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBoard()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupBoard() {
        binding.puzzleBoard.onPieceTapped = { pieceId ->
            viewModel.onEvent(PuzzleEvent.PieceClicked(pieceId))
        }
        binding.puzzleBoard.onPieceDropped = { pieceId, targetPosition ->
            viewModel.onEvent(PuzzleEvent.MovePiece(pieceId, targetPosition))
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Back)
        }
        binding.btnPause.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Pause)
        }
        binding.btnUndo.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Undo)
        }
        binding.btnRedo.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Redo)
        }
        binding.btnRotate.setOnClickListener {
            viewModel.uiState.value.selectedPieceId?.let { pieceId ->
                viewModel.onEvent(PuzzleEvent.RotatePiece(pieceId))
            }
        }
        binding.btnHint.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Hint)
        }
        binding.btnRestart.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Restart)
        }
        binding.btnResume.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Resume)
        }
        binding.btnPauseRestart.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Restart)
        }
        binding.btnPauseQuit.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Back)
        }
        binding.btnRetry.setOnClickListener {
            viewModel.onEvent(PuzzleEvent.Restart)
        }
        binding.btnChooseAnother.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnResultHome.setOnClickListener {
            navigateHome()
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

    private fun render(state: PuzzleUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.errorText.isVisible = state.isError

        val playing = !state.isLoading && !state.isError && !state.isCompleted
        binding.puzzleBoard.isVisible = playing || state.isCompleted
        binding.controlsRow.isVisible = playing
        binding.hudRow.isVisible = playing
        binding.btnPause.isVisible = playing

        if (state.isLoading) return

        binding.puzzleTitle.text = state.puzzleTitle
        state.imageId?.let { imageId ->
            binding.puzzleBoard.setImageResource(BasicPuzzleImages.imageResFor(imageId))
        }
        binding.puzzleBoard.render(
            puzzle = state.puzzle,
            selectedPieceId = state.selectedPieceId,
            hintPieceId = state.hintPieceId
        )

        binding.hudTimer.text = getString(R.string.hud_timer_format, state.elapsedTimeMs.formatDuration())
        binding.hudMoves.text = getString(R.string.hud_moves_format, state.moves)
        binding.hudHints.text = getString(R.string.hud_hints_format, state.hintsRemaining)

        binding.btnUndo.isEnabled = state.canUndo
        binding.btnRedo.isEnabled = state.canRedo
        binding.btnRotate.isEnabled = state.selectedPieceId != null && state.allowRotate
        binding.btnHint.isEnabled = state.hintsRemaining > 0

        binding.pauseOverlay.isVisible = state.isPaused

        renderCompletion(state)
    }

    private fun renderCompletion(state: PuzzleUiState) {
        binding.completionOverlay.isVisible = state.isCompleted
        val result = state.result ?: return
        binding.resultPuzzleName.text = state.puzzleTitle
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

    private fun handleEffect(effect: PuzzleEffect) {
        when (effect) {
            PuzzleEffect.NavigateBackToSelection -> findNavController().popBackStack()
            PuzzleEffect.NavigateHome -> navigateHome()
        }
    }

    private fun navigateHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }
}
