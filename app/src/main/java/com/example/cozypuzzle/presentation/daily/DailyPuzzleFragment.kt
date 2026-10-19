package com.example.cozypuzzle.presentation.daily

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
import com.example.cozypuzzle.core.customview.DailyPuzzleBoardView
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.BrushPalette
import com.example.cozypuzzle.databinding.FragmentDailyPuzzleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DailyPuzzleFragment :
    BaseFragment<FragmentDailyPuzzleBinding>(FragmentDailyPuzzleBinding::inflate) {

    private val viewModel: DailyPuzzleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBoard()
        setupPalette()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupBoard() {
        binding.dailyBoard.onDrawingInput = { input ->
            when (input) {
                is DailyPuzzleBoardView.DrawingInput.StrokeStarted ->
                    viewModel.onEvent(DailyPuzzleEvent.StrokeStarted(input.point.x, input.point.y))
                is DailyPuzzleBoardView.DrawingInput.StrokeExtended ->
                    viewModel.onEvent(DailyPuzzleEvent.StrokeExtended(input.points))
                is DailyPuzzleBoardView.DrawingInput.StrokeEnded ->
                    viewModel.onEvent(DailyPuzzleEvent.StrokeEnded)
            }
        }
    }

    private fun setupPalette() {
        binding.colorPalette.onColorSelected = { brush ->
            viewModel.onEvent(DailyPuzzleEvent.ColorSelected(brush.id))
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { viewModel.onEvent(DailyPuzzleEvent.Back) }
        binding.btnClear.setOnClickListener { viewModel.onEvent(DailyPuzzleEvent.ClearStroke) }
        binding.btnRestart.setOnClickListener { viewModel.onEvent(DailyPuzzleEvent.Restart) }
        binding.btnResultHome.setOnClickListener { navigateHome() }
        binding.btnRetry.setOnClickListener { viewModel.onEvent(DailyPuzzleEvent.Restart) }
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

    private fun render(state: DailyPuzzleUiState) {
        binding.progressBar.isVisible = state.isLoading
        binding.errorText.isVisible = state.isError
        if (state.isLoading || state.isError) return

        binding.dailyTitle.text = state.puzzleTitle
        binding.dailyDifficulty.setText(state.difficultyLabelRes)

        binding.hudTimer.text =
            getString(R.string.hud_timer_format, state.elapsedTimeMs.formatDuration())
        binding.hudAttempts.text = getString(R.string.daily_attempts_format, state.attempts)

        binding.colorPalette.submit(
            brushes = BrushPalette.brushes,
            selectedId = state.selectedBrushId
        )

        val brush = BrushPalette.byIdOrNull(state.selectedBrushId) ?: BrushPalette.default()
        binding.dailyBoard.render(
            boardSize = state.boardSize,
            startCell = state.startCell,
            goalCell = state.goalCell,
            obstacles = state.obstacleCells,
            pathPoints = state.pathPoints,
            brushColor = brush,
            isDrawing = state.isDrawing,
            isCompleted = state.isCompleted,
            inputEnabled = state.acceptsInput || state.isDrawing
        )

        binding.statusHint.setText(
            when {
                state.isCompleted -> R.string.daily_status_done
                state.pathPoints.isEmpty() -> R.string.daily_status_start_hint
                else -> R.string.daily_status_draw_hint
            }
        )
        binding.statusHint.setTextColor(
            if (state.isFailing) {
                ContextCompat.getColor(requireContext(), R.color.cozy_blossom_dark)
            } else {
                ContextCompat.getColor(requireContext(), R.color.cozy_green_dark)
            }
        )
        binding.controlsRow.isVisible = !state.isCompleted
        binding.completionOverlay.isVisible = state.isCompleted

        if (state.isCompleted) renderCompletion(state)
    }

    private fun renderCompletion(state: DailyPuzzleUiState) {
        val result = state.result ?: return
        binding.resultTime.text =
            getString(R.string.result_time_format, result.durationMs.formatDuration())
        binding.resultAttempts.text = getString(R.string.daily_result_attempts_format, result.attempts)
        binding.resultColor.text =
            getString(R.string.daily_result_color_format, BrushPalette.byId(result.brushColorId).name)
    }

    private fun handleEffect(effect: DailyPuzzleEffect) {
        when (effect) {
            DailyPuzzleEffect.ShowFailure -> {
                binding.dailyBoard.playFailureFeedback()
                binding.statusHint.setText(R.string.daily_status_hit_obstacle)
            }
            DailyPuzzleEffect.NavigateBack -> findNavController().popBackStack()
        }
    }

    private fun navigateHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }
}
