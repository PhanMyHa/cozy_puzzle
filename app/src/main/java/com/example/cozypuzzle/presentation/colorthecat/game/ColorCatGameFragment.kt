package com.example.cozypuzzle.presentation.colorthecat.game

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.data.datasource.BundledColorCatDataSource
import com.example.cozypuzzle.databinding.FragmentColorCatGameBinding
import com.example.cozypuzzle.presentation.colorthecat.select.ColorCatLevelSelectFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ColorCatGameFragment :
    BaseFragment<FragmentColorCatGameBinding>(FragmentColorCatGameBinding::inflate) {

    private val viewModel: ColorCatGameViewModel by viewModels()
    private lateinit var paletteAdapter: ColorCatPaletteAdapter

    private var pauseDialog: Dialog? = null
    private var completeDialog: Dialog? = null
    private var resetConfirmDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val levelId = arguments?.getString(ColorCatLevelSelectFragment.KEY_LEVEL_ID)
            ?: BundledColorCatDataSource.FIRST_LEVEL_ID

        if (savedInstanceState == null) {
            viewModel.loadLevel(levelId)
        }

        setupViews()
        observeUiState()
        observeEffects()
    }

    private fun setupViews() {
        paletteAdapter = ColorCatPaletteAdapter { color ->
            viewModel.onColorSelected(color)
        }
        binding.recyclerPalette.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recyclerPalette.adapter = paletteAdapter

        binding.svgColoringView.onRegionClicked = { regionId ->
            viewModel.onRegionClicked(regionId)
        }

        binding.btnGameBack.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnGamePause.setOnClickListener {
            viewModel.onPauseClicked()
        }

        binding.btnToolUndo.setOnClickListener {
            viewModel.onUndoClicked()
        }

        binding.btnToolReset.setOnClickListener {
            showResetConfirmationDialog()
        }

        binding.btnToolHint.setOnClickListener {
            viewModel.onHintClicked()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val game = state.gameState ?: return@collect

                    if (binding.svgColoringView.level != game.level) {
                        binding.svgColoringView.level = game.level
                        paletteAdapter.submitColors(game.level.paletteColors, game.selectedColor)
                    } else {
                        paletteAdapter.setSelectedColor(game.selectedColor)
                    }

                    binding.svgColoringView.regionColors = game.regionColors
                    binding.svgColoringView.highlightedRegionId = game.highlightedRegionId

                    binding.tvGameLevelTitle.text = game.level.title
                    binding.tvGameTimer.text = state.formattedTime
                    binding.tvGameProgress.text = getString(
                        R.string.color_cat_progress_format,
                        game.coloredRequiredRegionsCount,
                        game.totalRequiredRegions
                    )
                    binding.tvHintBadge.text = game.hintsRemaining.toString()

                    binding.btnToolUndo.isEnabled = state.canUndo
                    binding.btnToolUndo.alpha = if (state.canUndo) 1.0f else 0.4f
                }
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        ColorCatGameEffect.ShowPauseDialog -> showPauseDialog()
                        ColorCatGameEffect.DismissPauseDialog -> dismissPauseDialog()
                        is ColorCatGameEffect.ShowLevelComplete -> showLevelCompleteDialog(effect)
                        ColorCatGameEffect.NavigateToLevels -> {
                            findNavController().popBackStack()
                        }
                        is ColorCatGameEffect.NavigateToNextLevel -> {
                            viewModel.onNextLevelClicked(effect.levelId)
                        }
                        is ColorCatGameEffect.ShowMessage -> {
                            Snackbar.make(binding.root, effect.messageRes, Snackbar.LENGTH_SHORT).show()
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
            title = getString(R.string.color_cat_paused_title),
            onResume = { viewModel.onResumeClicked() },
            onRestart = { viewModel.onRestartClicked() },
            onExit = { viewModel.onExitClicked() }
        )
    }

    private fun dismissPauseDialog() {
        pauseDialog?.dismiss()
        pauseDialog = null
    }

    private fun showResetConfirmationDialog() {
        resetConfirmDialog?.dismiss()
        resetConfirmDialog = CozyDialogs.showConfirmDialog(
            context = requireContext(),
            title = getString(R.string.color_cat_reset_confirm_title),
            message = getString(R.string.color_cat_reset_confirm_message),
            positiveButtonText = getString(R.string.color_cat_btn_reset),
            onPositive = { viewModel.onConfirmReset() }
        )
    }

    private fun showLevelCompleteDialog(effect: ColorCatGameEffect.ShowLevelComplete) {
        completeDialog?.dismiss()
        val speech = if (effect.addedStars > 0) {
            getString(R.string.dialog_complete_reward_added, effect.addedStars)
        } else {
            getString(R.string.dialog_complete_reward_none)
        }
        completeDialog = CozyDialogs.showLevelCompleteDialog(
            context = requireContext(),
            stars = effect.stars,
            stat1 = CozyDialogs.StatSlot(
                value = getString(R.string.cat_puzzle_progress_format, effect.stars),
                label = getString(R.string.color_cat_stat_cats_painted)
            ),
            stat2 = CozyDialogs.StatSlot(
                value = effect.formattedTime,
                label = getString(R.string.color_cat_stat_time)
            ),
            stat3 = CozyDialogs.StatSlot(
                value = "💡 ${effect.hintsUsed}",
                label = getString(R.string.color_cat_stat_hints)
            ),
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
        resetConfirmDialog?.dismiss()
        resetConfirmDialog = null
        super.onDestroyView()
    }
}
