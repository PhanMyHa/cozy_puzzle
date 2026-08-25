package com.example.cozypuzzle.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cozypuzzle.R
import com.example.cozypuzzle.core.ui.BaseFragment
import com.example.cozypuzzle.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeUiState()
        observeEffects()
    }

    private fun setupListeners() {
        binding.btnEditName.setOnClickListener {
            viewModel.onEvent(ProfileEvent.EditNameClicked)
        }
        binding.btnSettings.setOnClickListener {
            viewModel.onEvent(ProfileEvent.SettingsClicked)
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

    private fun render(state: ProfileUiState) {
        val playerName = state.playerName.ifBlank { getString(R.string.player_default_name) }
        binding.profileTitle.text = playerName
        binding.profileLevel.text = getString(R.string.profile_level_format, state.level)
        binding.profileCoins.text = getString(R.string.coins_format, state.coins)
    }

    private fun handleEffect(effect: ProfileEffect) {
        when (effect) {
            ProfileEffect.FeatureComingSoon ->
                Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT)
                    .show()
        }
    }
}
