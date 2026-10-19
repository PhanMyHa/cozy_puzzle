package com.example.cozypuzzle.presentation.findpair.game

import android.app.Dialog
import android.os.Bundle
import android.view.View
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
import com.example.cozypuzzle.data.datasource.BundledFindPairDataSource
import com.example.cozypuzzle.databinding.FragmentFindPairGameBinding
import com.example.cozypuzzle.presentation.findpair.select.FindPairLevelSelectFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FindPairGameFragment :
    BaseFragment<FragmentFindPairGameBinding>(FragmentFindPairGameBinding::inflate) {

    private val viewModel: FindPairGameViewModel by viewModels()
    private lateinit var cardAdapter: FindPairCardAdapter

    private var pauseDialog: Dialog? = null
    private var gameOverDialog: Dialog? = null
    private var completeDialog: Dialog? = null
    private var resetDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val levelId = arguments?.getString(FindPairLevelSelectFragment.KEY_LEVEL_ID)
            ?: BundledFindPairDataSource.FIRST_LEVEL_ID

        if (savedInstanceState == null) {
            viewModel.loadLevel(levelId)
        }

        setupViews()
        observeUiState()
        observeEffects()
    }

    private fun setupViews() {
        cardAdapter = FindPairCardAdapter { card ->
            viewModel.onCardClicked(card.id)
        }
        binding.recyclerCards.adapter = cardAdapter
        binding.recyclerCards.layoutManager = GridLayoutManager(requireContext(), 3)

        binding.btnGameBack.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnGamePause.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnActionPause.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnActionReset.setOnClickListener {
            showResetConfirmationDialog()
        }

        binding.btnActionHint.setOnClickListener {
            viewModel.onHintClicked()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val game = state.gameState ?: return@collect

                    binding.tvGameLevelTitle.text = game.level.title
                    binding.tvGameDifficulty.text = when (game.level.difficulty) {
                        FindPairDifficulty.EASY -> getString(R.string.find_pair_tab_easy)
                        FindPairDifficulty.MEDIUM -> getString(R.string.find_pair_tab_medium)
                        FindPairDifficulty.HARD -> getString(R.string.find_pair_tab_hard)
                    }

                    binding.tvGamePairs.text = "${game.matchedPairCount}/${game.totalPairs}"
                    binding.tvGameTimer.text = state.formattedTimer
                    binding.tvGameFlips.text = getString(
                        R.string.find_pair_timer_flips_format,
                        game.flipsCount
                    )
                    binding.tvHintBadge.text = game.hintsRemaining.toString()

                    val currentLayoutManager = binding.recyclerCards.layoutManager as? GridLayoutManager
                    if (currentLayoutManager?.spanCount != game.level.columns) {
                        binding.recyclerCards.layoutManager =
                            GridLayoutManager(requireContext(), game.level.columns)
                    }

                    cardAdapter.submitList(game.cards)
                    cardAdapter.setHintedCards(game.hintedCardIds)
                }
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        FindPairGameEffect.ShowPauseDialog -> showPauseDialog()
                        FindPairGameEffect.DismissPauseDialog -> dismissPauseDialog()
                        FindPairGameEffect.ShowGameOverDialog -> showGameOverDialog()
                        is FindPairGameEffect.ShowLevelComplete -> showLevelCompleteDialog(effect)
                        FindPairGameEffect.NavigateToLevels -> {
                            findNavController().popBackStack()
                        }
                        is FindPairGameEffect.NavigateToNextLevel -> {
                            viewModel.onNextLevelClicked(effect.levelId)
                        }
                        is FindPairGameEffect.ShowMessage -> {
                            Snackbar.make(binding.root, effect.messageRes, Snackbar.LENGTH_SHORT).show()
                        }
                        is FindPairGameEffect.AnimateCardFlip -> {
                            cardAdapter.animateFlip(effect.cardId, effect.toFaceUp)
                        }
                        is FindPairGameEffect.AnimateCardsMatched -> {
                            cardAdapter.animateMatched(effect.cardId1, effect.cardId2)
                        }
                    }
                }
            }
        }
    }

    private fun showPauseDialog() {
        if (pauseDialog?.isShowing == true) return
        pauseDialog = CozyDialogs.showPauseDialog(
            context = requireContext(),
            title = getString(R.string.find_pair_paused_title),
            onResume = { viewModel.onResumeClicked() },
            onRestart = { viewModel.onPauseRestartClicked() },
            onExit = { viewModel.onExitClicked() }
        )
    }

    private fun dismissPauseDialog() {
        pauseDialog?.dismiss()
        pauseDialog = null
    }

    private fun showGameOverDialog() {
        gameOverDialog?.dismiss()
        gameOverDialog = CozyDialogs.showGameOverDialog(
            context = requireContext(),
            title = getString(R.string.find_pair_game_over_title),
            speechText = getString(R.string.find_pair_game_over_speech),
            onRetry = { viewModel.onRetryGameOver() },
            onQuit = { findNavController().popBackStack() }
        )
    }

    private fun showResetConfirmationDialog() {
        resetDialog?.dismiss()
        resetDialog = CozyDialogs.showConfirmDialog(
            context = requireContext(),
            title = getString(R.string.find_pair_reset_confirm_title),
            message = getString(R.string.find_pair_reset_confirm_message),
            positiveButtonText = getString(R.string.find_pair_btn_reset),
            negativeButtonText = getString(R.string.find_pair_btn_cancel),
            onPositive = { viewModel.onConfirmReset() },
            onNegative = { viewModel.dismissResetConfirmation() }
        )
    }

    private fun showLevelCompleteDialog(effect: FindPairGameEffect.ShowLevelComplete) {
        completeDialog?.dismiss()
        val speech = if (effect.addedStars > 0) {
            getString(R.string.dialog_complete_reward_added, effect.addedStars)
        } else {
            getString(R.string.dialog_complete_reward_none)
        }
        completeDialog = CozyDialogs.showLevelCompleteDialog(
            context = requireContext(),
            stars = effect.stars,
            stat1 = CozyDialogs.StatSlot("🐾 ${effect.pairsFound}/${effect.totalPairs}", getString(R.string.find_pair_stat_pairs)),
            stat2 = CozyDialogs.StatSlot(effect.formattedTime, getString(R.string.find_pair_stat_time)),
            stat3 = CozyDialogs.StatSlot(getString(R.string.find_pair_flips_format, effect.flips), getString(R.string.find_pair_stat_flips)),
            speechText = speech,
            nextLevelId = effect.nextLevelId,
            onNextLevel = { viewModel.onNextLevelClicked(effect.nextLevelId!!) },
            onBackToLevels = { findNavController().popBackStack() }
        )
    }

    override fun onDestroyView() {
        pauseDialog?.dismiss()
        pauseDialog = null
        gameOverDialog?.dismiss()
        gameOverDialog = null
        completeDialog?.dismiss()
        completeDialog = null
        resetDialog?.dismiss()
        resetDialog = null
        super.onDestroyView()
    }
}
