package com.example.cozypuzzle.presentation.catpuzzle.game

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import androidx.annotation.DrawableRes
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
import com.example.cozypuzzle.databinding.FragmentCatPuzzleGameBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CatPuzzleGameFragment :
    BaseFragment<FragmentCatPuzzleGameBinding>(FragmentCatPuzzleGameBinding::inflate) {

    private val viewModel: CatPuzzleGameViewModel by viewModels()

    private val trayAdapter = CatPuzzleTrayAdapter { pieceId ->
        viewModel.onPieceSelected(pieceId)
    }

    private var currentBitmap: Bitmap? = null
    private var currentImageRes: Int? = null

    private var pauseDialog: Dialog? = null
    private var completeDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBoard()
        setupTray()
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupBoard() {
        binding.puzzleBoard.onSlotTapped = { slotIndex ->
            viewModel.onSlotTapped(slotIndex)
        }
        binding.puzzleBoard.onPiecePlacedOrMoved = { pieceId, fromSlot, targetSlot ->
            viewModel.onPieceDropped(pieceId, fromSlot, targetSlot)
        }
    }

    private fun setupTray() {
        binding.trayRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.trayRecycler.adapter = trayAdapter

        // Allow dragging pieces from the board back to the tray
        binding.layoutTrayContainer.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DROP -> {
                    val clipText = event.clipData?.getItemAt(0)?.text?.toString()
                    if (clipText != null && clipText.startsWith("BOARD:")) {
                        val parts = clipText.split(":")
                        val fromSlot = parts.getOrNull(1)?.toIntOrNull()
                        if (fromSlot != null) {
                            viewModel.onPieceReturnedToTray(fromSlot)
                            true
                        } else false
                    } else false
                }
                else -> true
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            viewModel.onExitClicked()
        }
        binding.btnPauseHeader.setOnClickListener {
            viewModel.onPauseClicked()
        }
        binding.btnPauseBottom.setOnClickListener {
            viewModel.onPauseClicked()
        }
        binding.btnHint.setOnClickListener {
            viewModel.onHintClicked()
        }
        binding.btnShuffle.setOnClickListener {
            viewModel.onShuffleClicked()
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

    private fun render(state: CatPuzzleGameUiState) {
        val level = state.level ?: return
        val puzzle = state.puzzleState ?: return

        binding.tvLevelTitle.text = level.title
        binding.tvTimer.text = formatDuration(state.elapsedTimeMs)
        binding.tvStarProgress.text = "${puzzle.correctlyPlacedCount}/${puzzle.totalSlots}"

        // Load board bitmap for slicing
        if (currentImageRes != level.imageRes || currentBitmap == null) {
            currentImageRes = level.imageRes
            currentBitmap = createBitmapFromResource(level.imageRes)
            binding.puzzleBoard.setImageResource(level.imageRes)
            trayAdapter.setBoardBitmap(currentBitmap)
        }

        binding.puzzleBoard.setSlots(puzzle.slots)
        trayAdapter.submitList(puzzle.trayPieces)
        trayAdapter.setSelectedPieceId(puzzle.selectedPieceId)

        binding.btnHint.text = "${getString(R.string.cat_puzzle_btn_hint)} (${puzzle.hintsRemaining})"
        binding.btnHint.isEnabled = puzzle.hintsRemaining > 0 && !state.isCompleted && !state.isPaused

        if (state.isPaused) {
            showPauseDialog()
        } else {
            dismissPauseDialog()
        }

        if (state.isCompleted) {
            showCompleteDialog(state)
        } else {
            dismissCompleteDialog()
        }
    }

    private fun showPauseDialog() {
        if (pauseDialog?.isShowing == true) return
        pauseDialog = CozyDialogs.showPauseDialog(
            context = requireContext(),
            title = getString(R.string.cat_puzzle_paused_title),
            onResume = { viewModel.onResumeClicked() },
            onRestart = { viewModel.onRestartClicked() },
            onExit = { viewModel.onExitClicked() }
        )
    }

    private fun dismissPauseDialog() {
        pauseDialog?.dismiss()
        pauseDialog = null
    }

    private fun showCompleteDialog(state: CatPuzzleGameUiState) {
        if (completeDialog?.isShowing == true) return
        val stars = state.starsEarned
        val speech = if (state.addedStars > 0) {
            getString(R.string.dialog_complete_reward_added, state.addedStars)
        } else {
            getString(R.string.dialog_complete_reward_none)
        }
        completeDialog = CozyDialogs.showLevelCompleteDialog(
            context = requireContext(),
            stars = stars,
            stat1 = CozyDialogs.StatSlot(
                value = "$stars/3",
                label = getString(R.string.cat_puzzle_stat_stars)
            ),
            stat2 = CozyDialogs.StatSlot(
                value = formatDuration(state.elapsedTimeMs),
                label = getString(R.string.cat_puzzle_stat_time)
            ),
            stat3 = CozyDialogs.StatSlot(
                value = "💡 ${state.puzzleState?.hintsUsed ?: 0}",
                label = getString(R.string.cat_puzzle_stat_hints)
            ),
            speechText = speech,
            nextLevelId = state.nextLevelId,
            onNextLevel = { viewModel.onNextLevelClicked() },
            onBackToLevels = { viewModel.onExitClicked() }
        )
    }

    private fun dismissCompleteDialog() {
        completeDialog?.dismiss()
        completeDialog = null
    }

    private fun handleEffect(effect: CatPuzzleGameEffect) {
        when (effect) {
            CatPuzzleGameEffect.NavigateBack -> findNavController().popBackStack()
            is CatPuzzleGameEffect.NavigateToNextLevel -> viewModel.loadLevel(effect.levelId)
            is CatPuzzleGameEffect.PlayCorrectAnimation -> {
                binding.puzzleBoard.playCorrectSlotAnimation(effect.slotIndex)
            }
            CatPuzzleGameEffect.PlaySuccessSoundOrHaptic -> {
                // Subtle feedback if needed
            }
        }
    }

    private fun createBitmapFromResource(@DrawableRes resId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(requireContext(), resId) ?: return null
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, BITMAP_SIZE, BITMAP_SIZE)
        drawable.draw(canvas)
        return bitmap
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        dismissPauseDialog()
        dismissCompleteDialog()
        super.onDestroyView()
    }

    companion object {
        private const val BITMAP_SIZE = 1080
    }
}
