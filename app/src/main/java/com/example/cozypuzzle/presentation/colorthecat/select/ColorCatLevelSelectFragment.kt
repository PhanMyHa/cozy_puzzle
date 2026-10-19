package com.example.cozypuzzle.presentation.colorthecat.select

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
import com.example.cozypuzzle.core.colorthecat.model.ColorCatDifficulty
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.core.ui.CozyDialogs
import com.example.cozypuzzle.databinding.FragmentColorCatLevelSelectBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ColorCatLevelSelectFragment :
    BaseFragment<FragmentColorCatLevelSelectBinding>(FragmentColorCatLevelSelectBinding::inflate) {

    private val viewModel: ColorCatLevelSelectViewModel by viewModels()
    private lateinit var adapter: ColorCatLevelAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeUiState()
        observeEffects()
    }

    private fun setupViews() {
        adapter = ColorCatLevelAdapter { item ->
            viewModel.onLevelClicked(item)
        }
        binding.recyclerLevels.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerLevels.adapter = adapter

        binding.btnBack.setOnClickListener {
            viewModel.onBackClicked()
        }

        binding.tabBasic.setOnClickListener {
            viewModel.selectDifficulty(ColorCatDifficulty.BASIC)
        }
        binding.tabIntermediate.setOnClickListener {
            Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT).show()
        }
        binding.tabHard.setOnClickListener {
            Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvTotalStars.text = state.totalStars.toString()
                    adapter.submitList(state.levels)
                }
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is ColorCatLevelSelectEffect.NavigateToGame -> {
                            val bundle = bundleOf(KEY_LEVEL_ID to effect.levelId)
                            findNavController().navigate(
                                R.id.action_color_cat_select_to_game,
                                bundle
                            )
                        }
                        is ColorCatLevelSelectEffect.ShowLockedDialog -> {
                            showLockedDialog(effect.levelNumber)
                        }
                        ColorCatLevelSelectEffect.NavigateBack -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun showLockedDialog(levelNumber: Int) {
        CozyDialogs.showLockedDialog(
            context = requireContext(),
            levelNumber = levelNumber,
            message = getString(R.string.color_cat_locked_message)
        )
    }

    companion object {
        const val KEY_LEVEL_ID = "level_id"
    }
}
