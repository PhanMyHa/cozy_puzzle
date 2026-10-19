package com.example.cozypuzzle.presentation.slidethecat.game

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.slidethecat.model.SlideCatDifficulty
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.data.datasource.BundledSlideCatDataSource
import com.example.cozypuzzle.databinding.FragmentSlideCatGameBinding
import com.example.cozypuzzle.presentation.slidethecat.select.SlideCatLevelSelectFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SlideCatGameFragment :
    BaseFragment<FragmentSlideCatGameBinding>(FragmentSlideCatGameBinding::inflate) {

    private val viewModel: SlideCatGameViewModel by viewModels()

    private var pauseDialog: Dialog? = null
    private var completeDialog: Dialog? = null
    private var restartConfirmDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val levelId = arguments?.getString(SlideCatLevelSelectFragment.KEY_LEVEL_ID)
            ?: BundledSlideCatDataSource.FIRST_LEVEL_ID

        if (savedInstanceState == null) {
            viewModel.loadLevel(levelId)
        }

        setupViews()
        observeUiState()
        observeEffects()
    }

    private fun setupViews() {
        binding.slideCatBoardView.onTileClicked = { slotIndex ->
            viewModel.onTileClicked(slotIndex)
        }

        binding.btnGameBack.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnGamePause.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnActionPause.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnActionRestart.setOnClickListener {
            showRestartConfirmationDialog()
        }

        binding.btnActionHint.setOnClickListener {
            viewModel.onHintClicked()
        }

        binding.cardReferenceThumbnail.setOnClickListener {
            viewModel.toggleReferenceOverlay()
        }

        binding.layoutReferenceOverlay.setOnClickListener {
            viewModel.toggleReferenceOverlay()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val game = state.gameState ?: return@collect

                    binding.slideCatBoardView.gridSize = game.level.gridSize
                    binding.slideCatBoardView.setImageResource(game.level.imageRes)
                    binding.slideCatBoardView.showTileNumbers = game.showTileNumbersHint
                    binding.slideCatBoardView.isCompleted = game.isCompleted
                    binding.slideCatBoardView.setBoardState(game.board)

                    binding.ivReferenceThumbnail.setImageResource(game.level.thumbnailRes)
                    binding.ivReferenceFull.setImageResource(game.level.imageRes)
                    binding.layoutReferenceOverlay.visibility =
                        if (game.isReferenceOverlayVisible) View.VISIBLE else View.GONE

                    binding.tvGameLevelTitle.text = game.level.title
                    binding.tvGameDifficulty.text = when (game.level.difficulty) {
                        SlideCatDifficulty.EASY -> getString(R.string.slide_cat_tab_easy)
                        SlideCatDifficulty.MEDIUM -> getString(R.string.slide_cat_tab_medium)
                        SlideCatDifficulty.HARD -> getString(R.string.slide_cat_tab_hard)
                    }

                    binding.tvGameMoves.text = getString(
                        R.string.slide_cat_timer_moves_format,
                        game.movesCount
                    )
                    binding.tvGameStars.text = getString(
                        R.string.cat_puzzle_progress_format,
                        game.starsEarned
                    )
                    binding.tvHintBadge.text = game.hintsRemaining.toString()
                }
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        SlideCatGameEffect.ShowPauseDialog -> showPauseDialog()
                        SlideCatGameEffect.DismissPauseDialog -> dismissPauseDialog()
                        is SlideCatGameEffect.ShowLevelComplete -> showLevelCompleteDialog(effect)
                        SlideCatGameEffect.NavigateToLevels -> {
                            findNavController().popBackStack()
                        }
                        is SlideCatGameEffect.NavigateToNextLevel -> {
                            viewModel.onNextLevelClicked(effect.levelId)
                        }
                        is SlideCatGameEffect.ShowMessage -> {
                            Snackbar.make(binding.root, effect.messageRes, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun showPauseDialog() {
        if (pauseDialog?.isShowing == true) return
        pauseDialog = com.example.cozypuzzle.core.ui.CozyDialogs.showPauseDialog(
            context = requireContext(),
            title = getString(R.string.slide_cat_paused_title),
            onResume = { viewModel.onResumeClicked() },
            onRestart = { viewModel.onPauseRestartClicked() },
            onExit = { viewModel.onExitClicked() }
        )
    }

    private fun dismissPauseDialog() {
        pauseDialog?.dismiss()
        pauseDialog = null
    }

    private fun showRestartConfirmationDialog() {
        restartConfirmDialog?.dismiss()
        restartConfirmDialog = com.example.cozypuzzle.core.ui.CozyDialogs.showConfirmDialog(
            context = requireContext(),
            title = getString(R.string.slide_cat_restart_confirm_title),
            message = getString(R.string.slide_cat_restart_confirm_message),
            positiveButtonText = getString(R.string.slide_cat_btn_restart),
            onPositive = { viewModel.onConfirmRestart() }
        )
    }

    private fun showLevelCompleteDialog(effect: SlideCatGameEffect.ShowLevelComplete) {
        completeDialog?.dismiss()
        val speech = if (effect.addedStars > 0) {
            getString(R.string.dialog_complete_reward_added, effect.addedStars)
        } else {
            getString(R.string.dialog_complete_reward_none)
        }
        completeDialog = com.example.cozypuzzle.core.ui.CozyDialogs.showLevelCompleteDialog(
            context = requireContext(),
            stars = effect.stars,
            stat1 = com.example.cozypuzzle.core.ui.CozyDialogs.StatSlot(getString(R.string.slide_cat_moves_format, effect.moves), getString(R.string.slide_cat_stat_cats_completed)),
            stat2 = com.example.cozypuzzle.core.ui.CozyDialogs.StatSlot(effect.formattedTime, getString(R.string.slide_cat_stat_time)),
            stat3 = com.example.cozypuzzle.core.ui.CozyDialogs.StatSlot("💡 ${effect.hintsUsed}", getString(R.string.slide_cat_stat_hints)),
            speechText = speech,
            nextLevelId = effect.nextLevelId,
            onNextLevel = { viewModel.onNextLevelClicked(effect.nextLevelId!!) },
            onBackToLevels = { findNavController().popBackStack() }
        )
    }

    override fun onDestroyView() {
        pauseDialog?.dismiss()
        pauseDialog = null
        completeDialog?.dismiss()
        completeDialog = null
        restartConfirmDialog?.dismiss()
        restartConfirmDialog = null
        super.onDestroyView()
    }
}
